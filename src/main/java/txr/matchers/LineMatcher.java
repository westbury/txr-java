package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.parser.Expr;
import txr.parser.Ident;
import txr.parser.Line;
import txr.parser.Node;
import txr.parser.TextNode;

/**
 * A matcher that matches a given line from the TXR file.
 * <P>
 * The given line must be something that can be matched to a line
 * of input.  For example the line cannot contain a vertical directive
 * or be an @(end) line.
 * 
 * @author Nigel
 *
 */
public class LineMatcher extends Matcher {

	TxrOptions options;
	
	private List<HorizontalMatcher> matchers = new ArrayList<>();
	
	public LineMatcher(DocumentMatcher docMatcher, Line line, TxrOptions options) {
		this.options = docMatcher.options;
		
		int i = 0;
		while (i < line.nodes.size()) {
			Node node = line.nodes.get(i);
			i++;
		
			if (node instanceof Ident) {
				Ident identNode = (Ident)node;
				
				if (identNode.regex == null) {
					// It's a negative match.  See 6.9.

					/*
					 * For any negative matcher, we create a matcher for the
					 * negative matcher and all following positive matchers,
					 * i.e. all following matchers up to either the end of the
					 * line or the next negative matcher. This composite matcher
					 * then acts as a positive matcher.
					 */
					List<HorizontalMatcher> followingPositiveMatchers = new ArrayList<>();

					if (i == line.nodes.size()) {
						// Nothing more on line, so add a EOL matcher
						followingPositiveMatchers.add(new EndOfLineMatcher());
					} else {
						Node textNode = line.nodes.get(i);
						if (textNode.isNegativeMatcher()) {
							throw new RuntimeException("Can't have two consecutive negative matchers");
						}
						followingPositiveMatchers.add(getMatcherFromNode(textNode));
						i++;
						while (i < line.nodes.size() && !(textNode = line.nodes.get(i)).isNegativeMatcher()) {
							followingPositiveMatchers.add(getMatcherFromNode(textNode));
							i++;
						}
						if (i == line.nodes.size()) {
							// Nothing more on line, so add a EOL matcher
							followingPositiveMatchers.add(new EndOfLineMatcher());
						}
					}
					HorizontalMatcher followingMatcher = new TextSequenceMatcher(followingPositiveMatchers);
					HorizontalMatcher variableMatcher = new VariableMatcher(identNode, followingMatcher);
					matchers.add(variableMatcher);
				} else {
					// There is a regex so this is a positive matcher
					
					HorizontalMatcher variableMatcher = new VariableMatcherWithRegex(identNode, identNode.regex);
					matchers.add(variableMatcher);
				}
			} else {
				matchers.add(getMatcherFromNode(node));
			}
		}
	}

	private HorizontalMatcher getMatcherFromNode(Node node) {
		if (node instanceof TextNode) {
			return new TextMatcher((TextNode)node, this.options.tabsAlsoMatchSingleSpace);
		} else if (node instanceof Ident) {
			Ident identNode = (Ident)node;
			if (identNode.regex == null) {
				// It should not be possible for this to happen.
				throw new RuntimeException("Unsupported node");
			}
			return new VariableMatcherWithRegex(identNode, identNode.regex);
		} else {
			/*
			 * This could happen if there is an Expr node that is either not a vertical directive or is on
			 * a line with other text (which prevents it being processed as a vertical directive).
			 */
			if (node instanceof Expr) {
				Expr expression = (Expr)node;
				throw new TxrException("An expression has been found.  This expression occurs on a line with other data (there are characters on the line outside the expression), yet this is not a supported horizontal directive,", expression.subExpressions, 0);
			} else {
				// It should not be possible for this to happen
				throw new RuntimeException("Unsupported node");
			}
		}
	}

	@Override
	public boolean match(LinesFromInputReader documentReader, MatchContext context) {
		if (documentReader.isEndOfFile()) {
			return false;
		}
		
		int start = documentReader.getCurrent();
		String line = documentReader.fetchLine();
		CharsFromInputLineReader reader = new CharsFromInputLineReader(line);
		int i = 0;
		while (i < matchers.size()) {
			HorizontalMatcher matcher = matchers.get(i);
				if (!matcher.match(reader, context.bindings)) {
					// Line cannot match
					documentReader.setCurrent(start);
					return false;
				}
			
			i++;
		}

		if (!reader.isEndOfLine()) {
			// There is unmatched stuff at the end of the line
			documentReader.setCurrent(start);
			return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		return "Match on line: " + matchers.toString();
	}
}

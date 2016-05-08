package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.parser.Ident;
import txr.parser.Line;
import txr.parser.Node;
import txr.parser.TextNode;

/**
 * A matcher that matches a given line from the TXR file.
 * 
 * The given line must be something that can be matched to a line
 * of input.  For example the line cannot contain a vertical directive
 * or, for example, be an @(end) line.
 * 
 * @author Nigel
 *
 */
public class LineMatcher extends Matcher {

	private List<HorizontalMatcher> matchers = new ArrayList<>();
	
	public LineMatcher(DocumentMatcher docMatcher, Line line) {
		for (Node node  : line.nodes) {
			if (node instanceof Ident) {
				Variable var = docMatcher.getVariable((Ident)node);
				matchers.add(var);
			} else if (node instanceof TextNode) {
				matchers.add(new TextMatcher((TextNode)node));
			}
		}
	}

	@Override
	public boolean match(LinesFromInputReader documentReader) {
		String line = documentReader.fetchLine();
		CharsFromInputLineReader reader = new CharsFromInputLineReader(line);
		int i = 0;
		do {
			HorizontalMatcher matcher = matchers.get(i);

			if (matcher.isNegativeMatcher()) {
				int start = reader.getCurrent();
				
				// For each position, see we can match following text components
				do {
					int j = reader.getCurrent();

					int k = i + 1;
					do {
						HorizontalMatcher followingMatcher = matchers.get(k);
						if (followingMatcher.isNegativeMatcher()) {
							// We're done.  We have a match

							// j points to the offset of the first bit of
							// following text, to variable to bound to text
							// up to j.
							((Variable)matcher).text = reader.substring(start, j);
							break;
						}

						// Match next piece of text
						if (followingMatcher.match(reader)) {
							// No match, so offset j cannot match
							break;
						}

						k++;
					} while (k < matchers.size());

					if (k == matchers.size()) {
						// Matched text to end of line
						((Variable)matcher).text = reader.substring(i,j);
					}


					reader.fetchChar();
				} while (!reader.isEOL());

			} else {
				// It's not a negative matcher
				if (!matcher.match(reader)) {
					// Line cannot match
					return false;
				}
			}
			
			i++;
		} while (i < matchers.size());

		return true;
	}

	public String toString() {
		return "Match on line: " + matchers.toString();
	}
}

package txr.matchers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import txr.parser.AST;
import txr.parser.Expr;
import txr.parser.Line;
import txr.parser.Parser;
import txr.parser.SubExpression;
import txr.parser.Symbol;
import txr.parser.TxrErrorInDocumentException;

public class DocumentMatcher {

	public TxrOptions options;

	private MatchSequence topLevelMatcher = new MatchSequence();

	/**
	 * 
	 * @param txrInputStream an input stream containing TXR source
	 * @param charsetName the encoding type used to convert bytes from the stream into characters
	 * @throws TxrErrorInDocumentException 
	 * @throws IllegalArgumentException if the specified character set does not exist
	 */
	public DocumentMatcher(InputStream txrInputStream, String charsetName) throws TxrErrorInDocumentException {
		this(txrInputStream, charsetName, new TxrOptions());
	}
	
	/**
	 * 
	 * @param txrInputStream an input stream containing TXR source
	 * @param charsetName the encoding type used to convert bytes from the stream into characters
	 * @param options allow alternative behavior that differs from the specification but is sometimes
	 * 			more useful
	 * @throws TxrErrorInDocumentException 
	 * @throws IllegalArgumentException if the specified character set does not exist
	 */
	public DocumentMatcher(InputStream txrInputStream, String charsetName, TxrOptions options) throws TxrErrorInDocumentException {
		this(buildAst(txrInputStream, charsetName), null);
		this.options = options == null ? new TxrOptions() : options;
	}
	
	private static AST buildAst(InputStream txrInputStream, String charsetName) throws TxrErrorInDocumentException {
		StringBuilder result = new StringBuilder("");

		try (Scanner scanner = new Scanner(txrInputStream, charsetName)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append("\n");
			}
		}

		Parser p = new Parser();
		AST ast = p.parse(result.toString());

		return ast;
	}

	public DocumentMatcher(AST ast) {
		this(ast, null);
	}
	
	public DocumentMatcher(AST ast, TxrOptions options) {
		this.options = options != null ? options : new TxrOptions();
		
		int lineIndex = 0;

		Stack<VerticalMatcher> processorStack = new Stack<>();
		VerticalMatcher processor = topLevelMatcher;

		do {
			Line line = ast.lineSequence.get(lineIndex);

			if (line.nodes.size() == 1 && line.nodes.get(0) instanceof Expr) {
				Expr expr = (Expr) line.nodes.get(0);
				if (expr.subExpressions.size() >= 1) {
					SubExpression subExpression1 = expr.subExpressions.get(0);
					if (subExpression1 instanceof Symbol) {
						Symbol symbol = (Symbol) subExpression1;

						switch (symbol.symbolText) {
						case "collect":
							CollectMatcher collectMatcher = new CollectMatcher(expr);
							processor.addNextMatcherInMatchSequence(collectMatcher);
							processorStack.push(processor);
							processor = collectMatcher;
							break;

						case "cases":
							CasesMatcher casesMatcher = new CasesMatcher(expr);
							processor.addNextMatcherInMatchSequence(casesMatcher);
							processorStack.push(processor);
							processor = casesMatcher;
							break;

						case "maybe":
							MaybeMatcher maybeMatcher = new MaybeMatcher(expr);
							processor.addNextMatcherInMatchSequence(maybeMatcher);
							processorStack.push(processor);
							processor = maybeMatcher;
							break;

						case "skip":
							SkipMatcher skipMatcher = new SkipMatcher(expr);
							processor.addNextMatcherInMatchSequence(skipMatcher);
							processorStack.push(processor);
							processor = skipMatcher;
							break;

						case "assert":
							AssertMatcher assertMatcher = new AssertMatcher(expr);
							processor.addNextMatcherInMatchSequence(assertMatcher);
							break;

						case "throw":
							ThrowMatcher throwMatcher = new ThrowMatcher(expr);
							processor.addNextMatcherInMatchSequence(throwMatcher);
							break;

						case "bind":
							BindMatcher bindMatcher = new BindMatcher(expr);
							processor.addNextMatcherInMatchSequence(bindMatcher);
							break;

						case "end":
							while (processor instanceof SkipMatcher) {
								/* If in the text following a @(skip) then
								 * this text is being put into the @(skip) content,
								 * so we really have to pop twice, first pop the @(skip)
								 * then pop the containing object which the @(end) is ending.
								 */
								processor = processorStack.pop();
							}
							processor = processorStack.pop();
							break;

						default:
							/*
							 * It is not a general directive. Maybe it is a specific
							 * directive that can appear only inside the block for a
							 * vertical directive, e.g. @(UNTIL) which can appear
							 * only inside the lines for @(COLLECT). So pass on to
							 * the processor for the current level.
							 */

							/*
							 * Pop out of any @(skip) directives first.
							 * For example, a @(skip) may be in a @(collect) but we then
							 * get to the @(until) directive. 
							 */
							while (processor instanceof SkipMatcher) {
								/* If in the text following a @(skip) then
								 * this text is being put into the @(skip) content,
								 * so we really have to pop twice, first pop the @(skip)
								 * then pop the containing object which the @(end) is ending.
								 */
								processor = processorStack.pop();
							}
							
							processor.addNextDirective(expr);	
						}

					} else {
						throw new RuntimeException("List found where first element is not a symbol.  Not sure this is allowed???");
					}
				} else {
					throw new RuntimeException("Empty list found.  Not sure this is allowed???");
				}
			} else {
				// Not a vertical directive
				Matcher lineMatcher = new LineMatcher(this, lineIndex, line, this.options);
				processor.addNextMatcherInMatchSequence(lineMatcher);
			}

			lineIndex++;
		} while (lineIndex < ast.lineSequence.size());
	}

	public class MatchPair {
		public MatcherResult matcherResults;
		public MatchResults results;

		public MatchPair(MatcherResult matcherResults, MatchResults results) {
			this.matcherResults = matcherResults;
			this.results = results;
		}
	}
	public MatchResults process(String [] inputText) {
		LinesFromInputReader reader = new LinesFromInputReader(inputText);
		MatchResults results = new MatchResultsBase();
		MatchContext context = new MatchContext(results);
		
		MatcherResult matched = topLevelMatcher.match(reader, context);

		// TODO check asserts
		
		// Can this be cleaned up a bit?  Perhaps bindings should be part
		// of MatcherResult?
		if (matched.isSuccess()) {
			return results;
		} else {
			return null;
		}
	}
	public MatchPair process2(String [] inputText) {
		LinesFromInputReader reader = new LinesFromInputReader(inputText);
		MatchResults results = new MatchResultsBase();
		MatchContext context = new MatchContext(results);
		
		MatcherResult matched = topLevelMatcher.match(reader, context);

		// TODO check asserts
		
		// Can this be cleaned up a bit?  Perhaps bindings should be part
		// of MatcherResult?
		return new MatchPair(matched, results);
	}

	/**
	 * Parses input text where the text is supplied as a single String
	 * with each line separated by '/n' or 
	 * @param inputText
	 * @return
	 */
	public MatchResults process(String inputText) {
		List<String> inputTextList = new ArrayList<>();

		try (Scanner scanner = new Scanner(inputText)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				inputTextList.add(line);
			}
		}

		String [] inputTextArray = inputTextList.toArray(new String[0]);

		return process(inputTextArray);
	}

	@Override
	public String toString() {
		return topLevelMatcher.toString();
	}

}

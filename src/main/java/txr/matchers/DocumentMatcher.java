package txr.matchers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Stack;

import txr.matchers.MatcherResult.TxrCommandExecution;
import txr.matchers.TxrState.LineState;
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
							CollectMatcher collectMatcher = new CollectMatcher(lineIndex, expr);
							processor.addNextMatcherInMatchSequence(collectMatcher);
							processorStack.push(processor);
							processor = collectMatcher;
							break;

						case "cases":
							CasesMatcher casesMatcher = new CasesMatcher(lineIndex, expr);
							processor.addNextMatcherInMatchSequence(casesMatcher);
							processorStack.push(processor);
							processor = casesMatcher;
							break;

						case "maybe":
							MaybeMatcher maybeMatcher = new MaybeMatcher(lineIndex, expr);
							processor.addNextMatcherInMatchSequence(maybeMatcher);
							processorStack.push(processor);
							processor = maybeMatcher;
							break;

						case "skip":
							SkipMatcher skipMatcher = new SkipMatcher(lineIndex, expr);
							processor.addNextMatcherInMatchSequence(skipMatcher);
							processorStack.push(processor);
							processor = skipMatcher;
							break;

						case "assert":
							AssertMatcher assertMatcher = new AssertMatcher(lineIndex, expr);
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
								processor.setTxrEndLineIndex(lineIndex);
								processor = processorStack.pop();
							}
							processor.setTxrEndLineIndex(lineIndex);
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
							
							processor.addNextDirective(lineIndex, expr);	
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
		public TxrState newState;

		public MatchPair(MatcherResult matcherResults, MatchResults results, TxrState newState) {
			this.matcherResults = matcherResults;
			this.results = results;
			this.newState = newState;
		}
	}
	public MatchResults process(String [] inputText) {
		LinesFromInputReader reader = new LinesFromInputReader(inputText);
		MatchResults results = new MatchResultsBase();
		MatchContext context = new MatchContext(results, null);
		
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
	
	/**
	 * 
	 * @param inputText
	 * @param state the debugging state, being the state needed in addition to the TXR source and the target data,
	 * 				 which is sent by the server to the client and returned as-is to the server on the next request.
	 * 				This state should always be null if the TXR source or the target data change.
	 * @param command an optional command, which will alter the state and result in changes in the match controls.
	 * @return a callback that creates controls, together with the updated state
	 */
	public MatchPair process2(String [] inputText, TxrState state, TxrCommandExecution command) {
		LinesFromInputReader reader = new LinesFromInputReader(inputText);
		MatchResults results = new MatchResultsBase();
		
		// Update the state
		if (command != null) {
			if (state == null) {
				state = new TxrState() {};
			}
			Optional<LineState> lineState = Arrays.stream(state.lineStates).filter(x -> x.txrLineNumber == command.getTxrLineNumber() && x.dataLineNumber == command.getDataLineNumber()).findAny();
			switch (command.getCommandId()) {
				case ExpectAnotherCollectMatch:
				{
					if (lineState.isPresent()) {
						lineState.get().showExtraUnmatched = true;
					} else {
						List<LineState> asMutableList = new ArrayList<LineState>(Arrays.asList(state.lineStates));
						LineState newState = state.new LineState(command.getTxrLineNumber(), command.getDataLineNumber());
						newState.showExtraUnmatched = true;
						asMutableList.add(newState);
						state.lineStates = asMutableList.toArray(new LineState[0]);
					}
				}
				break;
				case ExpectOptionalToMatch:
				{
					if (lineState.isPresent()) {
						lineState.get().showFailingMaybe = true;
					} else {
						List<LineState> asMutableList = new ArrayList<LineState>(Arrays.asList(state.lineStates));
						LineState newState = state.new LineState(command.getTxrLineNumber(), command.getDataLineNumber());
						newState.showFailingMaybe = true;
						asMutableList.add(newState);
						state.lineStates = asMutableList.toArray(new LineState[0]);
					}
				}
				break;
			}
		}
		
		MatchContext context = new MatchContext(results, state);
		
		MatcherResult matched = topLevelMatcher.match(reader, context);

		// TODO check asserts
		
		// Can this be cleaned up a bit?  Perhaps bindings should be part
		// of MatcherResult?
		return new MatchPair(matched, results, state);
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

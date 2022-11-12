package txr.matchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import txr.matchers.TxrState.LineState;
import txr.parser.Expr;

public class MaybeMatcher extends ParallelMatcher {

	private int txrEndLineIndex;

	public MaybeMatcher(int txrLineNumber, Expr expr) {
		super(txrLineNumber);
		
		KeywordValues keywordValues = new KeywordValues(expr);
		// No keywords here
		keywordValues.failIfUnusedKeywords();
	}

	@Override
	protected String getDirectiveName() {
		return "Maybe";
	}
	
	@Override
	public MatcherResult match(LinesFromInputReader reader, MatchContext context) {
		final int start = reader.getCurrent();
		int longest = start;
		List<MatcherResultPair> allMatcherResults = new ArrayList<>();
		
		// Get state for this maybe instance.
		LineState stateOfThisLine = context.getLineState(txrLineNumber, start); // Is this.txrLineNumber actually a line index?

		for (MatchSequence eachMatchSequence : content) {
			MatchContext subContext = new MatchContext(context.bindings, context.state);

			MatcherResult eachMatcherResult = eachMatchSequence.match(reader, subContext);
			if (eachMatcherResult.isSuccess()) {
				
				int endOfThisMatch = reader.getCurrent();
				if (endOfThisMatch > longest) {
					longest = endOfThisMatch;
				}
				
				// Reset for next one
				reader.setCurrent(start);
			} else {
				/*
				 * Check for inner exception first. If an inner exception causes an outer @(assert) to
				 * fail then really it is the inner exception we are interested in.
				 */
				if (eachMatcherResult.getFailedResult().isException()) {
					return new MatcherResult(new MatcherResultMaybeFailure(txrLineNumber, start, allMatcherResults, eachMatcherResult.getFailedResult()));
				}
			}
			
			/*
			 * Note that a @(maybe) can never fail unless an exception was thrown.
			 */

			allMatcherResults.add(new MatcherResultPair(eachMatchSequence.txrLineNumber, eachMatcherResult, stateOfThisLine));
		}

		reader.setCurrent(longest);
		
		return new MatcherResult(new MatcherResultMaybeSuccess(txrLineNumber, start, allMatcherResults));
	}

	@Override
	public void setTxrEndLineIndex(int txrLineIndex) {
		this.txrEndLineIndex = txrLineIndex;
	}

}

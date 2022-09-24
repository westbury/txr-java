package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.matchers.TxrState.LineState;
import txr.parser.Expr;

public class NoneMatcher extends ParallelMatcher {

	public NoneMatcher(int txrLineNumber, Expr expr) {
		super(txrLineNumber);
		
		KeywordValues keywordValues = new KeywordValues(expr);
		// No keywords here
		keywordValues.failIfUnusedKeywords();
	}

	@Override
	protected String getDirectiveName() {
		return "None";
	}
	
	@Override
	public MatcherResult match(LinesFromInputReader reader, MatchContext context) {
		int startOfCases = reader.getCurrent();
		
		// Get state for this collect instance.
		LineState stateOfThisLine = context.getLineState(this.txrLineNumber + 1, startOfCases);
		
		/*
		 * Look for a match, going through the cases in order. As soon as one of
		 * the cases matches, we are done and we fail. If none match, this matcher matches.
		 */
		List<MatcherResultFailedPair> failedMatches = new ArrayList<>();
		for (Pair eachAlternative : content) {
			MatchContext subContext = new MatchContext(context.bindings, context.state);
			
			MatcherResult eachResult = eachAlternative.sequence.match(reader, subContext);
			if (eachResult.isSuccess()) {
				/*
				 * As soon as we get a match, we output a failed result. We show to the user this match, even though it is back-tracked.
				 * (See, for example, the @(until) clause of a @(collect), that likewise is shown when matched even though it is back-tracked).
				 */
				MatcherResultSuccessPair successPair = new MatcherResultSuccessPair(eachAlternative.txrLineIndex, eachResult.getSuccessfulResult());
				return new MatcherResult(new MatcherResultNoneFailure(txrLineNumber, startOfCases, successPair));
			} else {
				/*
				 * The sub-sequence did not match.  Check only that
				 * the matching did not get as far as processing any @(assert)
				 * directives inside the sub-sequence.  If an @(assert)
				 * directive was processed then the failure to match is an error.
				 */
				TxrAssertException failedAssert = subContext.assertContext.checkMatchFailureIsOk(reader.getCurrent(), eachAlternative.sequence);
				if (failedAssert != null) {
					// This matcher fails because we got all match failures followed by an assert failure
					// Or should the following be passing 'context' instead of 'subContext'?????
					return new MatcherResult(new MatcherResultNoneException(txrLineNumber, startOfCases, failedMatches, subContext, failedAssert, null));
				} else {
					failedMatches.add(new MatcherResultFailedPair(eachAlternative.txrLineIndex, eachResult.getFailedResult()));
				}
			}
		}
		
		boolean showAllFailuresInNone = stateOfThisLine != null && stateOfThisLine.showAllFailuresInNone;
		
		// We succeed because they all failed (no assert failure)
		// 'Expected match failure due to a successful match inside'
		if (showAllFailuresInNone) {
			// Don't fail, always return an exception when a user action indicates an expected match.
			// A failure will result in back-tracking and re-matching which we don't want.
			return new MatcherResult(new MatcherResultNoneException(txrLineNumber, startOfCases, failedMatches, context, null, stateOfThisLine));
		}
		
		return new MatcherResult(new MatcherResultNoneSuccess(txrLineNumber, startOfCases, failedMatches, stateOfThisLine));
	}

	@Override
	public void setTxrEndLineIndex(int txrLineIndex) {
		// TODO Do we need this?
		
	}

}

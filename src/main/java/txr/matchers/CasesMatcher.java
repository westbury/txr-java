package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.parser.Expr;

public class CasesMatcher extends ParallelMatcher {

	public CasesMatcher(int txrLineNumber, Expr expr) {
		super(txrLineNumber);
		
		KeywordValues keywordValues = new KeywordValues(expr);
		// No keywords here
		keywordValues.failIfUnusedKeywords();
	}

	@Override
	protected String getDirectiveName() {
		return "Cases";
	}
	
	@Override
	public MatcherResult match(LinesFromInputReader reader, MatchContext context) {
		/*
		 * Look for a match, going through the cases in order. As soon as one of
		 * the cases matches, we are done. If none match, this matcher does not
		 * match.
		 */
		List<MatcherResultFailed> failedMatches = new ArrayList<>();
		for (MatchSequence eachMatchSequence : content) {
			MatchContext subContext = new MatchContext(context.bindings);
			
			MatcherResult eachResult = eachMatchSequence.match(reader, subContext);
			if (eachResult.isSuccess()) {
				// TODO: We probably should also pass the list of prior failures, because part of the
				// reason why it matched this case is because it did not match any of the prior cases.
				// Perhaps the user expected a prior case to match (which might affect bindings or lines
				// consumed).
				return new MatcherResult(new MatcherResultCaseSuccess(eachResult.getSuccessfulResult()));
			} else {
				/*
				 * The sub-sequence did not match.  Check only that
				 * the matching did not get as far as processing any @(assert)
				 * directives inside the sub-sequence.  If an @(assert)
				 * directive was processed then the failure to match is an error.
				 */
				TxrAssertException failedAssert = subContext.assertContext.checkMatchFailureIsOk(reader.getCurrent(), eachMatchSequence);
				if (failedAssert != null) {
					// This matcher fails because we got all match failures followed by an assert failure
					return new MatcherResult(new MatcherResultCaseException(txrLineNumber, reader.getCurrent(), failedMatches, failedAssert));
				} else {
					failedMatches.add(eachResult.getFailedResult());
				}
			}
		}
		
		// We fail because they all failed (no assert failure)
		return new MatcherResult(new MatcherResultCaseFailure(txrLineNumber, reader.getCurrent()));
	}

	@Override
	public void setTxrEndLineIndex(int txrLineIndex) {
		// TODO Do we need this?
		
	}

}

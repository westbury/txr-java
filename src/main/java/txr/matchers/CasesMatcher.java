package txr.matchers;

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
		for (MatchSequence eachMatchSequence : content) {
			MatchContext subContext = new MatchContext(context.bindings);
			
			MatcherResult eachResult = eachMatchSequence.match(reader, subContext);
			if (eachResult.isSuccess()) {
				return new MatcherResult(new MatcherResultCaseSuccess(eachResult.getSuccessfulResult()));
			} else {
				/*
				 * The sub-sequence did not match.  Check only that
				 * the matching did not get as far as processing any @(assert)
				 * directives inside the sub-sequence.  If an @(assert)
				 * directive was processed then the failure to match is an error.
				 */
				subContext.assertContext.checkMatchFailureIsOk(reader.getCurrent(), eachMatchSequence);
			}
		}
		
		return new MatcherResult(new MatcherResultCaseFailure(txrLineNumber, reader.getCurrent()));
	}

	@Override
	public void setTxrEndLineIndex(int txrLineIndex) {
		// TODO Do we need this?
		
	}

}

package txr.matchers;

public class AssertContext {

	private MatcherResultAssert assertMatcherResult = null;
	
	public void setMatchObligatory(MatcherResultAssert assertMatcherResult) {
		this.assertMatcherResult  = assertMatcherResult;
		
	}

	public TxrAssertException checkMatchFailureIsOk(int lineNumber, Matcher matcher) {
		if (assertMatcherResult != null) {
			assertMatcherResult.setFailed();
			return new TxrAssertException(assertMatcherResult, lineNumber, matcher);
		} else {
			return null;
		}
	}
}

package txr.matchers;

public class TxrAssertException {

	private String message;
	
	public TxrAssertException(MatcherResultAssert assertMatcherResult, int lineNumber, Matcher matcher) {
		this.message = "Assert " + assertMatcherResult.getDescription() + " fails. Line number " + lineNumber + " does not match " + matcher.toString() + ".";
	}

}

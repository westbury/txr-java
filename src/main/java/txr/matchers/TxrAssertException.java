package txr.matchers;

public class TxrAssertException {

	private String message;
	
	public TxrAssertException(int lineNumberOfAssert, int lineNumber, Matcher matcher) {
		this.message = "Assert on line " + lineNumberOfAssert + " fails. Line number " + lineNumber + " does not match " + matcher.toString() + ".";
	}

}

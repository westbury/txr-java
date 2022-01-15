package txr.matchers;

public class TxrAssertException extends Exception {

	private static final long serialVersionUID = 1L;

	public TxrAssertException(int lineNumberOfAssert, int lineNumber, Matcher matcher) {
		super("Assert on line " + lineNumberOfAssert + " fails. Line number " + lineNumber + " does not match " + matcher.toString() + ".");
	}

}

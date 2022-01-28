package txr.matchers;

public class AssertContext {

	private int lineNumberOfAssert = 0;
	
	public void setMatchObligatory(int lineNumberOfAssert) {
		this.lineNumberOfAssert  = lineNumberOfAssert;
		
	}

	public TxrAssertException checkMatchFailureIsOk(int lineNumber, Matcher matcher) {
		if (lineNumberOfAssert != 0) {
			return new TxrAssertException(lineNumberOfAssert, lineNumber, matcher);
		} else {
			return null;
		}
	}
}

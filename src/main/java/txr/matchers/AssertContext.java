package txr.matchers;

public class AssertContext {

	private int lineNumberOfAssert = 0;
	
	public void setMatchObligatory(int lineNumberOfAssert) {
		this.lineNumberOfAssert  = lineNumberOfAssert;
		
	}

	public void checkMatchFailureIsOk(int lineNumber, Matcher matcher) {
		if (lineNumberOfAssert != 0) {
			throw new TxrAssertException(lineNumberOfAssert, lineNumber, matcher);
		}
	}
}

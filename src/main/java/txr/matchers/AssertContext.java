package txr.matchers;

public class AssertContext {

	private int lineNumberOfAssert = 0;
	
	public void setMatchObligatory(int lineNumberOfAssert) {
		this.lineNumberOfAssert  = lineNumberOfAssert;
		
	}

	public void checkMatchFailureIsOk(int lineNumber, Matcher matcher) {
		if (lineNumberOfAssert != 0) {
			throw new RuntimeException("Assert on line " + lineNumberOfAssert + " fails. Line number " + lineNumber + " does not match " + matcher.toString() + ".");
		}
	}
}

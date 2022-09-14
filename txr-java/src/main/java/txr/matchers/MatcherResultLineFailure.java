package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultLineFailure extends MatcherResultFailed {

	public final int txrLineNumber;

	public final int lineNumber;
	
	public final String message;

	public MatcherResultLineFailure(int txrLineNumber, int lineNumber, String message) {
		this.txrLineNumber = txrLineNumber;
		this.lineNumber = lineNumber;
		this.message = message;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createMismatch(txrLineNumber, lineNumber, indentation, message);
		
	}

}

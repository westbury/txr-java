package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultLineFailure extends MatcherResultFailed {

	public final String message;

	public MatcherResultLineFailure(int txrLineNumber, int startLineNumber, String message) {
		super(txrLineNumber, startLineNumber);
		this.message = message;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createMismatch(txrLineNumber, startLineNumber, indentation, message);
		
	}

}

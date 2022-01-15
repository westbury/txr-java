package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultCaseFailure extends MatcherResultFailed {

	private int txrLineNumber;
	
	private int startLineNumber;

	public MatcherResultCaseFailure(int txrLineNumber, int startLineNumber) {
		this.txrLineNumber = txrLineNumber;
		this.startLineNumber = startLineNumber;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createMismatch(txrLineNumber, startLineNumber, indentation, "All cases failed to match");
	}

}

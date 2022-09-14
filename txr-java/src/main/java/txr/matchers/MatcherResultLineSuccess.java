package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultLineSuccess extends MatcherResultSuccess {

	public final int txrLineNumber;

	public final int lineNumber;

	public MatcherResultLineSuccess(int txrLineNumber, int lineNumber) {
		this.txrLineNumber = txrLineNumber;
		this.lineNumber = lineNumber;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createMatch(txrLineNumber, lineNumber, indentation);
	}

}

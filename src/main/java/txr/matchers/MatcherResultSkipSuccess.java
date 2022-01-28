package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultSkipSuccess extends MatcherResultSuccess {

	private int txrLineNumber;
	private int startLine;
	private int skippedToLine;
	private MatcherResultSuccess successfulResult;

	public MatcherResultSkipSuccess(int txrLineNumber, int startLine, int skippedToLine, MatcherResultSuccess successfulResult) {
		this.txrLineNumber = txrLineNumber;
		this.startLine = startLine;
		this.skippedToLine = skippedToLine;
		this.successfulResult = successfulResult;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createDirective(txrLineNumber, startLine, indentation);
		this.successfulResult.createControls(callback, indentation);
		
	}

}

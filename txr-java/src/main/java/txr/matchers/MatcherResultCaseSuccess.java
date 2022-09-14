package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;

public class MatcherResultCaseSuccess extends MatcherResultSuccess {

	private int txrLineNumber;
	private int startLineNumber;
	private MatcherResultSuccess successfulResult;

	public MatcherResultCaseSuccess(int txrLineNumber, int startLineNumber, MatcherResultSuccess successfulResult) {
		this.txrLineNumber = txrLineNumber;
		this.startLineNumber = startLineNumber;
		this.successfulResult = successfulResult;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createDirective(txrLineNumber, startLineNumber, indentation, new TxrAction[0]);
		this.successfulResult.createControls(callback, indentation + 1);
		
	}

}

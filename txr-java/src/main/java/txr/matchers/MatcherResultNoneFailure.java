package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;

public class MatcherResultNoneFailure extends MatcherResultFailed {

	private MatcherResultSuccessPair successfulResult;

	public MatcherResultNoneFailure(int txrLineNumber, int startLineNumber, MatcherResultSuccessPair successfulResult) {
		super(txrLineNumber, startLineNumber);
		this.successfulResult = successfulResult;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createDirective(txrLineNumber, startLineNumber, indentation, new TxrAction[0]);
		
		callback.createDirective(successfulResult.txrLineIndex, startLineNumber, indentation, new TxrAction[0]);
		successfulResult.successfulMatcher.createControls(callback, indentation + 1);
		callback.rewind(startLineNumber);
	}

}

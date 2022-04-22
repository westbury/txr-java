package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;

public class MatcherResultAssert extends MatcherResultSuccess {

	private int txrLineNumber;

	private int startLine;

	private boolean assertFailed = false;
	
	public MatcherResultAssert(int txrLineNumber, int startLine) {
		this.txrLineNumber = txrLineNumber;
		this.startLine = startLine;
	}

	public void setFailed() {
		assertFailed = true;
	}
	
	@Override
	public void createControls(IControlCallback callback, int indentation) {
		if (assertFailed) {
			callback.createDirectiveWithError(txrLineNumber, startLine, indentation);
		} else {
			callback.createDirective(txrLineNumber, startLine, indentation, new TxrAction[0]);
		}
	}

	public String getDescription() {
		return "on line " + txrLineNumber + " (matching  at line " + startLine + " in the data)" ;
	}

}

package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResultCaseFailure; // fixme

public class MatcherResultCaseException extends MatcherResultException {

	private int txrLineNumber;
	
	private int startLineNumber;

	private List<MatcherResultCaseFailure.Pair> failedMatches;

	private TxrAssertException failedAssert;

	public MatcherResultCaseException(int txrLineNumber, int startLineNumber, List<MatcherResultCaseFailure.Pair> failedMatches, TxrAssertException failedAssert) {
		this.txrLineNumber = txrLineNumber;
		this.startLineNumber = startLineNumber;
		this.failedMatches = failedMatches;
		this.failedAssert = failedAssert;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createMismatch(txrLineNumber, startLineNumber, indentation, "All cases failed to match");
	}

}

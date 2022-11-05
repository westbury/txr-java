package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultCaseException extends MatcherResultException {

	private List<MatcherResultFailedPair> failedMatches;

	private TxrAssertException failedAssert;

	public MatcherResultCaseException(int txrLineNumber, int startLineNumber, List<MatcherResultFailedPair> failedMatches, TxrAssertException failedAssert) {
		super(txrLineNumber, startLineNumber);
		this.failedMatches = failedMatches;
		this.failedAssert = failedAssert;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createMismatch(txrLineNumber, startLineNumber, indentation, "All cases failed to match");
	}

}

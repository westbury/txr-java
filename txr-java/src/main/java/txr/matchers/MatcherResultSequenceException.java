package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultSequenceException extends MatcherResultException {

	private List<MatcherResultSuccess> successfulMatches;

	private MatcherResultFailed failedMatch;

	private TxrAssertException failedAssert;

	public MatcherResultSequenceException(int txrLineNumber, int startLineNumber, List<MatcherResultSuccess> successfulMatches, MatcherResultFailed failedMatch, TxrAssertException failedAssert) {
		super(txrLineNumber, startLineNumber);
		this.successfulMatches = successfulMatches;
		this.failedMatch = failedMatch;
		this.failedAssert = failedAssert;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		for (MatcherResultSuccess successfulMatch : successfulMatches) {
			successfulMatch.createControls(callback, indentation);
		}
		failedMatch.createControls(callback, indentation);
		
		// TODO how do we return the failed assert?
		// Should this be done when the @(assert) directive itself is returned?
	}

}

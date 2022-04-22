package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;

public class MatcherResultCollectException extends MatcherResultException {

	private int txrLineNumber;
	private int startLine;

	private List<MatcherResultSuccess> successfulMatches;
	private MatcherResultFailed failedMatch;
	private MatcherResultSuccess lastMatch;
	private MatcherResultSuccess untilMatch;

//	private TxrAssertException failedAssert;

	public MatcherResultCollectException(int txrLineNumber, int startLine, List<MatcherResultSuccess> successfulMatches, MatcherResultSuccess lastMatch, MatcherResultSuccess untilMatch, MatcherResultFailed failedMatch) {
		this.txrLineNumber = txrLineNumber;
		this.startLine = startLine;
		this.successfulMatches = successfulMatches;
		this.lastMatch = lastMatch;
		this.untilMatch = untilMatch;
		this.failedMatch = failedMatch;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createDirective(txrLineNumber, startLine, indentation, new TxrAction[0]);

		for (MatcherResultSuccess successfulMatch : successfulMatches) {
			successfulMatch.createControls(callback, indentation);
		}
		failedMatch.createControls(callback, indentation);
		
		// TODO how do we return the failed assert?
		// Should this be done when the @(assert) directive itself is returned?
	}

}

package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultSkipFailure extends MatcherResultFailed {

	private int txrLineNumber;
	
	private int startLineNumber;

	int bestSkippedToLine;

	private MatcherResultFailed bestFailedMatch;

	public MatcherResultSkipFailure(int txrLineNumber, int startLineNumber, int bestSkippedToLine, MatcherResultFailed bestFailedMatch) {
		this.txrLineNumber = txrLineNumber;
		this.startLineNumber = startLineNumber;
		this.bestSkippedToLine = bestSkippedToLine;
		this.bestFailedMatch = bestFailedMatch;
	}

	@Override
	public boolean isException() {
		return bestFailedMatch.isException();
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createDirective(txrLineNumber, startLineNumber, indentation);
		bestFailedMatch.createControls(callback, indentation);
//		callback.createMismatch(txrLineNumber, startLineNumber, indentation, "All possible skips failed to match");
	}

}

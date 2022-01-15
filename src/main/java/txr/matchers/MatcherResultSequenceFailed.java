package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultSequenceFailed extends MatcherResultFailed {

	private List<MatcherResultSuccess> successfulMatches;
	private MatcherResultFailed failedMatch;

	public MatcherResultSequenceFailed(List<MatcherResultSuccess> successfulMatches, MatcherResultFailed failedMatch) {
		this.successfulMatches = successfulMatches;
		this.failedMatch = failedMatch;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		for (MatcherResultSuccess successfulMatch : successfulMatches) {
			successfulMatch.createControls(callback, indentation);
		}
		failedMatch.createControls(callback, indentation);
	}

}

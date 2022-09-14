package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultSequenceFailed extends MatcherResultFailed {

	List<MatcherResultSuccess> successfulMatches;
	MatcherResultFailed failedMatch;

	public MatcherResultSequenceFailed(List<MatcherResultSuccess> successfulMatches, MatcherResultFailed failedMatch) {
		this.successfulMatches = successfulMatches;
		this.failedMatch = failedMatch;
		this.score = successfulMatches.size() * 10 + failedMatch.getScore();
	}

	@Override
	public boolean isException() {
		return failedMatch.isException();
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		for (MatcherResultSuccess successfulMatch : successfulMatches) {
			successfulMatch.createControls(callback, indentation);
		}
		failedMatch.createControls(callback, indentation);
	}

}

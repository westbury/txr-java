package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultSequenceSuccess extends MatcherResultSuccess {

	private List<MatcherResultSuccess> successfulMatches;
	
	public MatcherResultSequenceSuccess(List<MatcherResultSuccess> successfulMatches) {
		this.successfulMatches = successfulMatches;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		for (MatcherResultSuccess successfulMatch : successfulMatches) {
			successfulMatch.createControls(callback, indentation);
		}
	}

}

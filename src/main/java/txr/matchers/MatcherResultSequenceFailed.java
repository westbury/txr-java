package txr.matchers;

import java.util.List;

public class MatcherResultSequenceFailed extends MatcherResultFailed {

	private List<MatcherResultSuccess> successfulMatches;
	private MatcherResultFailed failedMatch;

	public MatcherResultSequenceFailed(List<MatcherResultSuccess> successfulMatches, MatcherResultFailed failedMatch) {
		this.successfulMatches = successfulMatches;
		this.failedMatch = failedMatch;
	}

}

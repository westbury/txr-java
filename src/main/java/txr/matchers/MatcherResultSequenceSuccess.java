package txr.matchers;

import java.util.List;

public class MatcherResultSequenceSuccess extends MatcherResultSuccess {

	private List<MatcherResultSuccess> successfulMatches;
	
	public MatcherResultSequenceSuccess(List<MatcherResultSuccess> successfulMatches) {
		this.successfulMatches = successfulMatches;
	}

}

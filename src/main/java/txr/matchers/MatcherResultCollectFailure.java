package txr.matchers;

import java.util.List;

public class MatcherResultCollectFailure extends MatcherResultFailed {

	private String message;
	private List<MatcherResultSuccess> bodyMatchers;
	private MatcherResultSuccess lastMatch;
	private MatcherResultSuccess untilMatch;

	public MatcherResultCollectFailure(String message, List<MatcherResultSuccess> bodyMatchers,
			MatcherResultSuccess lastMatch, MatcherResultSuccess untilMatch) {
		this.message = message;
		this.bodyMatchers = bodyMatchers;
		this.lastMatch = lastMatch;
		this.untilMatch = untilMatch;
	}

}

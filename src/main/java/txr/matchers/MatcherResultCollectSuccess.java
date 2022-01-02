package txr.matchers;

import java.util.List;

public class MatcherResultCollectSuccess extends MatcherResultSuccess {

	private List<MatcherResultSuccess> bodyMatchers;
	private MatcherResultSuccess lastMatch;
	private MatcherResultSuccess untilMatch;

	public MatcherResultCollectSuccess(List<MatcherResultSuccess> bodyMatchers, MatcherResultSuccess lastMatch,
			MatcherResultSuccess untilMatch) {
		this.bodyMatchers = bodyMatchers;
		this.lastMatch = lastMatch;
		this.untilMatch = untilMatch;
	}

}

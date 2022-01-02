package txr.matchers;

import java.util.List;

public class MatcherResultMaybe extends MatcherResultSuccess {

	private int lineNumberStart;
	
	private List<MatcherResult> subClauseResults;

	public MatcherResultMaybe(int lineNumberStart, List<MatcherResult> subClauseResults) {
		this.lineNumberStart = lineNumberStart;
		this.subClauseResults = subClauseResults;
	}

}

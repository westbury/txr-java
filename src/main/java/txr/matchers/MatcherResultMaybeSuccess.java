package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultMaybeSuccess extends MatcherResultSuccess {

	private int lineNumberStart;
	
	private List<MatcherResult> subClauseResults;

	public MatcherResultMaybeSuccess(int lineNumberStart, List<MatcherResult> subClauseResults) {
		this.lineNumberStart = lineNumberStart;
		this.subClauseResults = subClauseResults;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		// To avoid clutter, don't show a mismatch if a sub-clause did not match
		
		for (MatcherResult subClauseResult : subClauseResults) {
			if (subClauseResult.isSuccess()) {
				subClauseResult.createControls(callback, indentation + 1);
			}
		}
		
	}

}

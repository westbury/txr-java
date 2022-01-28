package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;

/**
 * A @(maybe) can only fail if an exception is thrown.
 * 
 * @author Nigel
 *
 */
public class MatcherResultMaybeFailure extends MatcherResultFailed {

	private int lineNumberStart;
	
	private List<MatcherResult> subClauseResults;

	private MatcherResultFailed failedMatch;
/**
 * 
 * @param lineNumberStart
 * @param subClauseResults
 * @param failedMatch always an exception, as that is the only way @(maybe) can fail to match
 */
	public MatcherResultMaybeFailure(int lineNumberStart, List<MatcherResult> subClauseResults, MatcherResultFailed failedMatch) {
		this.lineNumberStart = lineNumberStart;
		this.subClauseResults = subClauseResults;
		this.failedMatch = failedMatch;
	}

	@Override
	public boolean isException() {
		return failedMatch.isException(); // always true
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		// First show the prior sub-clauses, up to the one that threw the exception.
		// To avoid clutter, don't show a mismatch if a sub-clause did not match
		
		for (MatcherResult subClauseResult : subClauseResults) {
			if (subClauseResult.isSuccess()) {
				subClauseResult.createControls(callback, indentation + 1);
			}
		}
		
		failedMatch.createControls(callback, indentation + 1);
	}

}
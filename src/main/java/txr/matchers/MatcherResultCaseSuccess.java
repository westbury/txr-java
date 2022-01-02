package txr.matchers;

public class MatcherResultCaseSuccess extends MatcherResultSuccess {

	private MatcherResultSuccess successfulResult;

	public MatcherResultCaseSuccess(MatcherResultSuccess successfulResult) {
		this.successfulResult = successfulResult;
	}

}

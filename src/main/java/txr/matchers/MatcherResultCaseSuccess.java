package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultCaseSuccess extends MatcherResultSuccess {

	private MatcherResultSuccess successfulResult;

	public MatcherResultCaseSuccess(MatcherResultSuccess successfulResult) {
		this.successfulResult = successfulResult;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		this.successfulResult.createControls(callback, indentation + 1);
		
	}

}

package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultBind extends MatcherResultSuccess {

	private int startLine;

	public MatcherResultBind(int startLine) {
		this.startLine = startLine;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		// TODO Figure out how to show bindings to the user
		
	}

}

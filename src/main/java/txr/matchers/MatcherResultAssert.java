package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultAssert extends MatcherResultSuccess {

	private int startLine;

	public MatcherResultAssert(int startLine) {
		this.startLine = startLine;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		// TODO Auto-generated method stub
		
	}

}

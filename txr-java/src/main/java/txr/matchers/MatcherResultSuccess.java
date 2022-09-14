package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public abstract class MatcherResultSuccess {

	public abstract void createControls(IControlCallback callback, int indentation);

}

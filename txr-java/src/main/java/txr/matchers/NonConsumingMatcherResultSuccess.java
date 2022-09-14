package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public abstract class NonConsumingMatcherResultSuccess {

	public abstract void createControls(IControlCallback callback, int indentation);

}

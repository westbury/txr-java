package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public abstract class MatcherResultException extends MatcherResultFailed {

	@Override
	public boolean isException() {
		return true;
	}

}

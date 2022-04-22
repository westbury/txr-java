package txr.matchers;

public abstract class MatcherResultException extends MatcherResultFailed {

	@Override
	public boolean isException() {
		return true;
	}

}

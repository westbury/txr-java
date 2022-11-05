package txr.matchers;

public abstract class MatcherResultException extends MatcherResultFailed {

	protected MatcherResultException(int txrLineNumber, int startLineNumber) {
		super(txrLineNumber, startLineNumber);
	}

	@Override
	public boolean isException() {
		return true;
	}

}

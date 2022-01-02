package txr.matchers;

public class MatcherResultCaseFailure extends MatcherResultFailed {

	private int startLineNumber;

	public MatcherResultCaseFailure(int startLineNumber) {
		this.startLineNumber = startLineNumber;
	}

}

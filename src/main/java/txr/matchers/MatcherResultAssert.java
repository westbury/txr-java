package txr.matchers;

public class MatcherResultAssert extends MatcherResultSuccess {

	private int startLine;

	public MatcherResultAssert(int startLine) {
		this.startLine = startLine;
	}

}

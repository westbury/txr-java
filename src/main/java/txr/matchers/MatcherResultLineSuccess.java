package txr.matchers;

public class MatcherResultLineSuccess extends MatcherResultSuccess {

	public final int txrLineNumber;

	public final int lineNumber;

	public MatcherResultLineSuccess(int txrLineNumber, int lineNumber) {
		this.txrLineNumber = txrLineNumber;
		this.lineNumber = lineNumber;
	}

}

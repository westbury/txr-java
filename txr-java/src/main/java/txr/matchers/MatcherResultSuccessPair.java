package txr.matchers;

public class MatcherResultSuccessPair {
	public final int txrLineIndex; // @case, @or, @and that precedes this one
	public final MatcherResultSuccess successfulMatcher;
	
	public MatcherResultSuccessPair(int txrLineIndex, MatcherResultSuccess successfulMatcher) {
		this.txrLineIndex = txrLineIndex;
		this.successfulMatcher = successfulMatcher;
	}
}
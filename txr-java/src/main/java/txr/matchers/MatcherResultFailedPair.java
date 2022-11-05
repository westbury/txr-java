package txr.matchers;

// TODO we don't need this class anymore
public class MatcherResultFailedPair {
	public final int txrLineIndex; // @case, @or, @and that precedes this one
	public final MatcherResultFailed failedMatcher;
	
	public MatcherResultFailedPair(int txrLineIndex, MatcherResultFailed failedMatcher) {
		this.txrLineIndex = txrLineIndex;
		this.failedMatcher = failedMatcher;
	}
}
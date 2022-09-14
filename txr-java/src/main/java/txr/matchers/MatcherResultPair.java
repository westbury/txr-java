package txr.matchers;

import txr.matchers.TxrState.LineState;

public class MatcherResultPair {
	public final int txrLineIndex; // @case, @maybe, @or, @and that precedes this one
	public final MatcherResult matcherResult;
	public final LineState stateOfThisLine;
	
	public MatcherResultPair(int txrLineIndex, MatcherResult matcherResult, LineState stateOfThisLine) {
		this.txrLineIndex = txrLineIndex;
		this.matcherResult = matcherResult;
		this.stateOfThisLine = stateOfThisLine;
	}
}
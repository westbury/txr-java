package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;

public class MatcherResultCaseFailure extends MatcherResultFailed {

	private int txrLineNumber;
	
	private int startLineNumber;

	private String message;

	private List<Pair> failedMatchers;

	public static class Pair {
		public final int txrLineIndex; // @case, @or, @and that precedes this one
		public final MatcherResultFailed failedMatcher;
		
		public Pair(int txrLineIndex, MatcherResultFailed failedMatcher) {
			this.txrLineIndex = txrLineIndex;
			this.failedMatcher = failedMatcher;
		}
	}
	
	public MatcherResultCaseFailure(int txrLineNumber, int startLineNumber, String message, List<Pair> failedMatchers) {
		this.txrLineNumber = txrLineNumber;
		this.startLineNumber = startLineNumber;
		this.message = message;
		this.failedMatchers = failedMatchers;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
// now done in loop
		//		callback.createDirective(txrLineNumber, startLineNumber, indentation, new TxrAction[0]);

		// Show anything that did match as that would be useful.
		for (Pair failedMatcher : failedMatchers) {
			callback.rewind(startLineNumber);
			callback.createDirective(failedMatcher.txrLineIndex, startLineNumber, indentation, new TxrAction[0]);
			failedMatcher.failedMatcher.createControls(callback, indentation + 1);
		}
	}

}

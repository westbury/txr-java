package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;

public class MatcherResultCaseFailure extends MatcherResultFailed {

	private int txrLineNumber;
	
	private int startLineNumber;

	private String message;

	private List<MatcherResultFailedPair> failedMatchers;

	public MatcherResultCaseFailure(int txrLineNumber, int startLineNumber, String message, List<MatcherResultFailedPair> failedMatchers) {
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
		for (MatcherResultFailedPair failedMatcher : failedMatchers) {
			callback.rewind(startLineNumber);
			callback.createDirective(failedMatcher.txrLineIndex, startLineNumber, indentation, new TxrAction[0]);
			failedMatcher.failedMatcher.createControls(callback, indentation + 1);
		}
	}

}

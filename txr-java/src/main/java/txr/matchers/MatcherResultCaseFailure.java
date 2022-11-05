package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;

public class MatcherResultCaseFailure extends MatcherResultFailed {

	private String message;

	private List<MatcherResultFailed> failedMatchers;

	public MatcherResultCaseFailure(int txrLineNumber, int startLineNumber, String message, List<MatcherResultFailed> failedMatchers) {
		super(txrLineNumber, startLineNumber);
		this.message = message;
		this.failedMatchers = failedMatchers;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
// now done in loop
		//		callback.createDirective(txrLineNumber, startLineNumber, indentation, new TxrAction[0]);

		// Show anything that did match as that would be useful.
		for (MatcherResultFailed failedMatcher : failedMatchers) {
			callback.rewind(startLineNumber);
			callback.createDirective(failedMatcher.txrLineNumber, startLineNumber, indentation, new TxrAction[0]);
			failedMatcher.createControls(callback, indentation + 1);
		}
	}

}

package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;

public class MatcherResultCollectFailure extends MatcherResultFailed {

	private int txrLineNumber;
	private int startLine;
	private String message;
	private List<MatcherResultSuccess> bodyMatchers;
	private MatcherResultSuccess lastMatch;
	private MatcherResultSuccess untilMatch;
	private MatcherResultFailed bestFailedMatch;

	public MatcherResultCollectFailure(int txrLineNumber, int startLine, String message, List<MatcherResultSuccess> bodyMatchers,
			MatcherResultSuccess lastMatch, MatcherResultSuccess untilMatch, MatcherResultFailed bestFailedMatch) {
		this.txrLineNumber = txrLineNumber;
		this.startLine = startLine;
		this.message = message;
		this.bodyMatchers = bodyMatchers;
		this.lastMatch = lastMatch;
		this.untilMatch = untilMatch;
		this.bestFailedMatch = bestFailedMatch;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createDirective(txrLineNumber, startLine, indentation, new TxrAction[0]);

		// Show anything that did match as that would be useful.
		for (MatcherResultSuccess bodyMatcher : bodyMatchers) {
			bodyMatcher.createControls(callback, indentation + 1);
		}

		if (bestFailedMatch != null) {
			// We know there should be another collect match but unexpectedly failed to match.
			// We know this because either mintimes was not reached or because the user
			// told us through a debug command.
			bestFailedMatch.createControls(callback, indentation + 1);
		}
		
		// Show the 'until' clause matching, but only if it matched on something
		if (untilMatch != null) {
			untilMatch.createControls(callback, indentation + 1);
		}

	}

}

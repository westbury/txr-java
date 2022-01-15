package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;

public class MatcherResultCollectFailure extends MatcherResultFailed {

	private int txrLineNumber;
	private int startLine;
	private String message;
	private List<MatcherResultSuccess> bodyMatchers;
	private MatcherResultSuccess lastMatch;
	private MatcherResultSuccess untilMatch;

	public MatcherResultCollectFailure(int txrLineNumber, int startLine, String message, List<MatcherResultSuccess> bodyMatchers,
			MatcherResultSuccess lastMatch, MatcherResultSuccess untilMatch) {
		this.txrLineNumber = txrLineNumber;
		this.startLine = startLine;
		this.message = message;
		this.bodyMatchers = bodyMatchers;
		this.lastMatch = lastMatch;
		this.untilMatch = untilMatch;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createMismatch(txrLineNumber, startLine, indentation, message);

		// Show anything that did match as that would be useful.
		for (MatcherResultSuccess bodyMatcher : bodyMatchers) {
			bodyMatcher.createControls(callback, indentation + 1);
		}

		// Show the 'until' clause matching, but only if it matched on something
		if (untilMatch != null) {
			untilMatch.createControls(callback, indentation + 1);
		}

	}

}

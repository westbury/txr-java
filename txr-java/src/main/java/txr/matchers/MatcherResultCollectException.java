package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.matchers.MatcherResult.CommandId;
import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;
import txr.matchers.TxrState.LineState;

public class MatcherResultCollectException extends MatcherResultException {

	private int txrLineNumber;
	private int startLine;

	private List<MatcherResultSuccess> successfulMatches;
	private MatcherResultFailed failedMatch;
	private MatcherResultSuccess lastMatch;
	private MatcherResultSuccess untilMatch;
	private MatchContext context;

//	private TxrAssertException failedAssert;

	public MatcherResultCollectException(int txrLineNumber, int startLine, List<MatcherResultSuccess> successfulMatches, MatcherResultSuccess lastMatch, MatcherResultSuccess untilMatch, MatcherResultFailed failedMatch, MatchContext context) {
		this.txrLineNumber = txrLineNumber;
		this.startLine = startLine;
		this.successfulMatches = successfulMatches;
		this.lastMatch = lastMatch;
		this.untilMatch = untilMatch;
		this.failedMatch = failedMatch;
		this.context = context;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		LineState stateOfThisLine = this.context.getLineState(this.txrLineNumber + 1, startLine);

		boolean showExtraUnmatched = stateOfThisLine != null && stateOfThisLine.showExtraUnmatched;

		List<TxrAction> actions = new ArrayList<>();
		if (showExtraUnmatched) {
			actions.add(new TxrAction() {
				@Override
				public String getLabel() {
					return "OK, don't expect another collect match here afterall";
				}
				@Override
				public CommandId getId() {
					return CommandId.ExpectAnotherCollectMatch;
				}
				@Override
				public boolean isClearingCommand() {
					return true;
				}
			});
		}
		callback.createDirective(txrLineNumber, startLine, indentation, actions.toArray(new TxrAction[0]));

		for (MatcherResultSuccess successfulMatch : successfulMatches) {
			successfulMatch.createControls(callback, indentation + 1);
		}
		failedMatch.createControls(callback, indentation + 1);
		
		// TODO how do we return the failed assert?
		// Should this be done when the @(assert) directive itself is returned?
	}

}

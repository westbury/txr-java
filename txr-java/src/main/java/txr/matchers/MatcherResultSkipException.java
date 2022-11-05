package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.matchers.MatcherResult.CommandId;
import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;
import txr.matchers.TxrState.LineState;

/**
 * This class represents the situation where a @(none) matches because none of the sub-clauses match
 * but the user expects one of the sub-clauses to match and this clause to fail.
 * 
 * We the user selects this state, all failing cases are shown.
 * 
 * This class is also used when an @(assert) is inside a sub-clause of a @(none). This is a rather pointless
 * @(assert), but we support it nonetheless. (What does the reference implementation do?)
 */
public class MatcherResultSkipException extends MatcherResultException {

	/** these will all be failed matches, not exceptions */
	private List<MatcherResultFailed> priorAttempts;

	/** this is always an exception */
	private MatcherResultFailed failedResult;
	
	private MatchContext context;

	private LineState stateOfThisLine;
	
	public MatcherResultSkipException(int txrLineNumber, int startLineNumber, int skippedToLine,
			MatcherResultFailed failedResult, List<MatcherResultFailed> priorAttempts, MatchContext context, LineState stateOfThisLine) {
		super(txrLineNumber, startLineNumber);
		this.failedResult = failedResult;
		this.priorAttempts = priorAttempts;
		this.context = context;
		this.stateOfThisLine = stateOfThisLine;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		int showSkippingToThisLine = stateOfThisLine == null ? -1 : stateOfThisLine.showSkippingToThisLine;

		List<TxrAction> actions = new ArrayList<>();
		if (showSkippingToThisLine != -1) {
			TxrAction action = new TxrAction() {
				@Override
				public String getLabel() {
					return "Actually, don't expect this one to skip to line " + showSkippingToThisLine + " after all";
				}
				@Override
				public CommandId getId() {
					return new CommandId.ExpectSkipToGivenLine(-1);
				}
				@Override
				public boolean isClearingCommand() {
					return true;
				}
			};
			actions.add(action);
		} else {
			// An assert failure occured. Now it is possible that the users thinks the assert
			// should not have occured because the @(skip) should have matched to an earlier line.
			// Therefore we show menu options that allow the user to force an earlier match.
			for (MatcherResultFailed attempt : priorAttempts) {
				TxrAction action = new TxrAction() {
					@Override
					public String getLabel() {
						return "Expect match when skipping to line " + (attempt.getLineNumber() + 1);
					}
					@Override
					public CommandId getId() {
						return new CommandId.ExpectSkipToGivenLine(attempt.getLineNumber());
					}
					@Override
					public boolean isClearingCommand() {
						return false;
					}
				};
				actions.add(action);
				if (actions.size() >= 10) break;
			}
		}
		
		callback.createDirective(txrLineNumber, startLineNumber, indentation, actions.toArray(new TxrAction[0]));
		
		failedResult.createControls(callback, indentation);
	}

}

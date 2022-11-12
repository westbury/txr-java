package txr.matchers;

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
public class MatcherResultNoneException extends MatcherResultException {

	private List<MatcherResultFailed> failedMatchers;

	private MatchContext context;

	private LineState stateOfThisLine;
	
	public MatcherResultNoneException(int txrLineNumber, int startLineNumber, List<MatcherResultFailed> failedMatchers, MatchContext context, TxrAssertException txrAssertException, LineState stateOfThisLine) {
		super(txrLineNumber, startLineNumber);
		this.failedMatchers = failedMatchers;
		this.context = context;
		this.stateOfThisLine = stateOfThisLine;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		boolean showAllFailuresInNone = stateOfThisLine != null && stateOfThisLine.showAllFailuresInNone;
		
		TxrAction action;
		assert showAllFailuresInNone;
		action = new TxrAction() {
			@Override
			public String getLabel() {
				return "Actually, don't expect this one to fails after all (ok that no subclauses match)";
			}
			@Override
			public CommandId getId() {
				return CommandId.ExpectNoneClauseToFail;
			}
			@Override
			public boolean isClearingCommand() {
				return true;
			}
			@Override
			public int getTxrLineNumber() {
				return txrLineNumber;
			}
			@Override
			public int getDataLineNumber() {
				return startLineNumber;
			}
		};
		TxrAction[] actions = { action };

		for (MatcherResultFailed failedMatcher : failedMatchers) {
			callback.rewind(startLineNumber);
			callback.createDirective(failedMatcher.txrLineNumber, startLineNumber, indentation, actions);
			failedMatcher.createControls(callback, indentation + 1);
			
			// Next time, for the @(or) lines, don't show any actions
			actions = new TxrAction[0];
		}
	}

}

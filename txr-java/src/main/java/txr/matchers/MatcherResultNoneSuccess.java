package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.CommandId;
import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;
import txr.matchers.TxrState.LineState;

public class MatcherResultNoneSuccess extends MatcherResultSuccess {

	private int txrLineNumber;
	private int startLineNumber;
	private List<MatcherResultFailed> failedMatchers;
	private LineState stateOfThisLine;

	public MatcherResultNoneSuccess(int txrLineNumber, int startLineNumber, List<MatcherResultFailed> failedMatchers, LineState stateOfThisLine) {
		this.txrLineNumber = txrLineNumber;
		this.startLineNumber = startLineNumber;
		this.failedMatchers = failedMatchers;
		this.stateOfThisLine = stateOfThisLine;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		boolean showAllFailuresInNone = stateOfThisLine != null && stateOfThisLine.showAllFailuresInNone;
		
		TxrAction action;
		assert !showAllFailuresInNone;
		action = new TxrAction() {
			@Override
			public String getLabel() {
				return "Expected this one to fail (ie subclause matches)";
			}
			@Override
			public CommandId getId() {
				return CommandId.ExpectNoneClauseToFail;
			}
			@Override
			public boolean isClearingCommand() {
				return false;
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

		callback.createDirective(txrLineNumber, startLineNumber, indentation, actions);
		
	}

}

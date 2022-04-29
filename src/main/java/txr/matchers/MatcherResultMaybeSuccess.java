package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.CommandId;
import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;

public class MatcherResultMaybeSuccess extends MatcherResultSuccess {

	private int txrLineNumber;
	
	private int startLineNumber;
	
	private List<MatcherResultPair> subClauseResults;

	public MatcherResultMaybeSuccess(int txrLineNumber, int startLineNumber, List<MatcherResultPair> allMatcherResults) {
		this.txrLineNumber = txrLineNumber;
		this.startLineNumber = startLineNumber;
		this.subClauseResults = allMatcherResults;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		// To avoid clutter, we don't show a mismatch if a sub-clause did not match.
		// However we do show the @(or) or @(and) directives. These directive lines contain
		// the action that the user can use to indicate that a match was expected.	
		
		for (MatcherResultPair subClauseResult : subClauseResults) {
			callback.rewind(startLineNumber); // only needed if multiple sub-clauses

			if (subClauseResult.matcherResult.isSuccess()) {
				callback.createDirective(subClauseResult.txrLineIndex, startLineNumber, indentation, new TxrAction[0]);
				subClauseResult.matcherResult.createControls(callback, indentation + 1);
			} else {
				// If a mismatch then we add the action
				TxrAction[] actions = {
						new TxrAction() {
							@Override
							public String getLabel() {
								return "Expected this one to be a match here";
							}
							@Override
							public CommandId getId() {
								return CommandId.ExpectOptionalToMatch;
							}
						}
				};
				callback.createDirective(subClauseResult.txrLineIndex, startLineNumber, indentation, actions);
				
				if (subClauseResult.stateOfThisLine != null && subClauseResult.stateOfThisLine.showFailingMaybe) {
					subClauseResult.matcherResult.createControls(callback, indentation + 1);
				}
			}
		}
	}
}

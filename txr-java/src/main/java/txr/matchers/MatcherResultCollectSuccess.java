package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.CommandId;
import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;
import txr.matchers.TxrState.LineState;

public class MatcherResultCollectSuccess extends MatcherResultSuccess {

	private int txrLineNumber;
	private int startLine;
	private int untilTxrLineNumber;
	private int untilLine;
	private int endTxrLineNumber;
	private int endLine;
	private List<MatcherResultSuccess> bodyMatchers;
	private MatcherResultSuccess lastMatch;
	private MatcherResultSuccess untilMatch;
	private MatchContext context;

	public MatcherResultCollectSuccess(int txrLineNumber, int startLine, int untilTxrLineNumber, int untilLine, int endTxrLineNumber, int endLine, List<MatcherResultSuccess> bodyMatchers, MatcherResultSuccess lastMatch,
			MatcherResultSuccess untilMatch, MatchContext context) {
		this.txrLineNumber = txrLineNumber;
		this.startLine = startLine;
		this.untilTxrLineNumber = untilTxrLineNumber;
		this.untilLine = untilLine;
		this.endTxrLineNumber = endTxrLineNumber;
		this.endLine = endLine;
		this.bodyMatchers = bodyMatchers;
		this.lastMatch = lastMatch;
		this.untilMatch = untilMatch;
		this.context = context;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		
		// Get state for this collect instance.
		LineState stateOfThisLine = this.context.getLineState(this.txrLineNumber + 1, startLine);

		boolean showExtraUnmatched = stateOfThisLine != null && stateOfThisLine.showExtraUnmatched;

		assert !showExtraUnmatched; // this would be an 'exception' instance, not a 'success' instance, if showExtraUnmatched set
		TxrAction[] actions = {
			new TxrAction() {
				@Override
				public String getLabel() {
					return "Expect another collect match here";
				}
				@Override
				public CommandId getId() {
					return CommandId.ExpectAnotherCollectMatch;
				}
				@Override
				public boolean isClearingCommand() {
					return false;
				}
			}
		};
		callback.createDirective(txrLineNumber, startLine, indentation, actions);
		
		for (MatcherResultSuccess bodyMatcher : bodyMatchers) {
			bodyMatcher.createControls(callback, indentation + 1);
		}

		// Show the 'until' clause matching, but only if it matched on something
		if (untilMatch != null) {
			callback.createDirective(untilTxrLineNumber, untilLine, indentation, new TxrAction[0]);
			
			// The matching that occurs in the 'until' clause is unusual.
			// No bindings occur (check against reference impl) and the data
			// are not consumed (input data that matched the 'until' must still be
			// matched after the end of the collect).
			
			untilMatch.createControls(callback, indentation + 1);
			callback.rewind(untilLine);
		}

		callback.createDirective(endTxrLineNumber, endLine, indentation, new TxrAction[0]);
	}

}

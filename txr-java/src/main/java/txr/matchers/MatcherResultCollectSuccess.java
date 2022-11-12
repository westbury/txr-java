package txr.matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import txr.matchers.MatcherResult.CommandId;
import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;
import txr.matchers.TxrState.LineState;

public class MatcherResultCollectSuccess extends MatcherResultSuccess {

	private final class TxrActionExpectingAnotherCollect implements TxrAction {
		private int txrLineNumber;
		private int dataLineNumber;

		TxrActionExpectingAnotherCollect(int txrLineNumber, int dataLineNumber) {
			this.txrLineNumber = txrLineNumber;
			this.dataLineNumber = dataLineNumber;
		}
		
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

		@Override
		public int getTxrLineNumber() {
			return txrLineNumber;
		}

		@Override
		public int getDataLineNumber() {
			return dataLineNumber;
		}
	}

	private final class TxrActionExpectingUntilToMatch implements TxrAction {
		private int txrLineNumber;
		private int dataLineNumber;

		TxrActionExpectingUntilToMatch(int txrLineNumber, int dataLineNumber) {
			this.txrLineNumber = txrLineNumber;
			this.dataLineNumber = dataLineNumber;
		}
		
		@Override
		public String getLabel() {
			return "Expect @(until) to match";
		}

		@Override
		public CommandId getId() {
			return CommandId.ExpectAnotherCollectMatch;
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
			return dataLineNumber;
		}
	}

	private int txrLineIndex;
	private int startLine;
	private Integer untilTxrLineIndex; // null if no @(until) clause
	private int untilLine;
	private int endTxrLineIndex;
	private int endLine;
	private List<MatcherResultSuccess> bodyMatchers;
	private MatcherResultSuccess lastMatch;
	private MatcherResultSuccess untilMatch;
	private MatchContext context;

	/**
	 * A list of all the attempts at matching the @(until) clause, or null
	 * if there is no @(until) clause
	 */
	private List<MatcherResultFailed> untilAttempts;


	/**
	 * 
	 * @param txrLineIndex
	 * @param startLine
	 * @param untilTxrLineIndex null if no @(until) clause
	 * @param untilLine
	 * @param endTxrLineIndex
	 * @param endLine
	 * @param bodyMatchers
	 * @param lastMatch
	 * @param untilMatch
	 * @param untilAttempts list of all the attempts at matching the @(until) clause, or null
	 * 		if there is no @(until) clause
	 * @param context
	 */
	public MatcherResultCollectSuccess(int txrLineIndex, int startLine, Integer untilTxrLineIndex, int untilLine, int endTxrLineIndex, int endLine, List<MatcherResultSuccess> bodyMatchers, MatcherResultSuccess lastMatch,
			MatcherResultSuccess untilMatch, List<MatcherResultFailed> untilAttempts, MatchContext context) {
		this.txrLineIndex = txrLineIndex;
		this.startLine = startLine;
		this.untilTxrLineIndex = untilTxrLineIndex;
		this.untilLine = untilLine;
		this.endTxrLineIndex = endTxrLineIndex;
		this.endLine = endLine;
		this.bodyMatchers = bodyMatchers;
		this.lastMatch = lastMatch;
		this.untilMatch = untilMatch;
		this.untilAttempts = untilAttempts;
		this.context = context;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		
		// Get state for this collect instance.
		LineState stateOfThisLine = this.context.getLineState(txrLineIndex, startLine);

		boolean showExtraUnmatched = stateOfThisLine != null && stateOfThisLine.showExtraUnmatched;
		int showUntilMatchingThisLine = stateOfThisLine == null ? -1 : stateOfThisLine.showUntilMatchingThisLine;

		assert !showExtraUnmatched; // this would be an 'exception' instance, not a 'success' instance, if showExtraUnmatched set
		TxrAction[] actions = {
			new TxrActionExpectingAnotherCollect(txrLineIndex, startLine)
		};
		callback.createDirective(txrLineIndex, startLine, indentation, actions);
		
		for (MatcherResultSuccess bodyMatcher : bodyMatchers) {
			bodyMatcher.createControls(callback, indentation + 1);
		}

		// Show the 'until' clause matching, but only if it matched on something
		if (untilTxrLineIndex != null) {
			
			List<TxrAction> actions2 = new ArrayList<>();
			
			if (showUntilMatchingThisLine != -1) {
				// Actually will never get here because it would be an exception result
			} else {
				// For now, take the best 10.  We could be cleverer, for example if there were 11 with the same
				// good score and the rest with the same worse score, then returning 11 would make sense.
				
				Collections.sort(untilAttempts, new Comparator<MatcherResultFailed>() {
					@Override
					public int compare(MatcherResultFailed attemptA, MatcherResultFailed attemptB) {
						return attemptB.getScore() - attemptA.getScore();
					}
				});
				
				for (MatcherResultFailed attempt : untilAttempts) {
					TxrAction action2 = new TxrAction() {
						@Override
						public String getLabel() {
							return "Expect @(until) clause to match at line " + (attempt.getLineNumber() + 1);
						}
						@Override
						public CommandId getId() {
							return new CommandId.ExpectUntilToMatchGivenLine(attempt.getLineNumber());
						}
						@Override
						public boolean isClearingCommand() {
							return false;
						}
						@Override
						public int getTxrLineNumber() {
							return txrLineIndex;
						}
						@Override
						public int getDataLineNumber() {
							return startLine;
						}
					};
					actions2.add(action2);
					if (actions2.size() >= 10) break;
				}
			}

			if (untilMatch != null) {
				callback.createDirective(untilTxrLineIndex, untilLine, indentation, new TxrAction[0]);
				
				// The matching that occurs in the 'until' clause is unusual.
				// No bindings occur (check against reference impl) and the data
				// are not consumed (input data that matched the 'until' must still be
				// matched after the end of the collect).
				
				untilMatch.createControls(callback, indentation + 1);
				callback.rewind(untilLine);
			} else {
				// There is an @(until) clause but it did not match anything.
				// Show it positioned on the end line so the @(until) and the @(end) are right
				// next to each other., together with a menu item that
				// allows the user to force match it.

				callback.createDirective(
						untilTxrLineIndex, endLine, indentation, actions2.toArray(new TxrAction[0]) /*
						new TxrAction[] { new TxrActionExpectingUntilToMatch() } */
				);
			}
		}

		callback.createDirective(endTxrLineIndex, endLine, indentation, new TxrAction[0]);
	}

}

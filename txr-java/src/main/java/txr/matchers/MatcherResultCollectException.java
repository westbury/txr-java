package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.matchers.MatcherResult.CommandId;
import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;
import txr.matchers.TxrState.LineState;

public class MatcherResultCollectException extends MatcherResultException {

	private List<MatcherResultSuccess> successfulMatches;
	
	/**
	 * the match in the collect body that caused the exception,
	 * or null if this is a forced match on the @(until) clause
	 */
	private MatcherResultFailed failedMatch;
	
	private MatcherResultSuccess lastMatch;
	private MatcherResult untilMatch;
	private MatchContext context;

//	private TxrAssertException failedAssert;

	public MatcherResultCollectException(int txrLineIndex, int startLineNumber, List<MatcherResultSuccess> successfulMatches, MatcherResultSuccess lastMatch, MatcherResultSuccess untilMatch, MatcherResultFailed failedMatch, MatchContext context) {
		super(txrLineIndex, startLineNumber);
		this.successfulMatches = successfulMatches;
		this.lastMatch = lastMatch;
		this.untilMatch = new MatcherResult(untilMatch);
		this.failedMatch = failedMatch;
		this.context = context;
	}

	/**
	 * This form of the constructor is used when the user has forced the @(until) clause
	 * to attempt a match at a given line.
	 * 
	 * @param txrLineIndex
	 * @param startOfCollect
	 * @param successfulMatches
	 * @param lastMatch
	 * @param untilMatch usually this would be a failed match, but could potentially be a successful
	 * 			match if the user has made edits to make it a match
	 * @param context
	 */
	public MatcherResultCollectException(int txrLineIndex, int startLineNumber, List<MatcherResultSuccess> successfulMatches,
			MatcherResultSuccess lastMatch, MatcherResult untilMatch, MatchContext context) {
		super(txrLineIndex, startLineNumber);
		this.successfulMatches = successfulMatches;
		this.lastMatch = lastMatch;
		this.untilMatch = untilMatch;
		this.failedMatch = null;
		this.context = context;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		LineState stateOfThisLine = this.context.getLineState(txrLineNumber, startLineNumber);

		boolean showExtraUnmatched = stateOfThisLine != null && stateOfThisLine.showExtraUnmatched;

		List<TxrAction> actions = new ArrayList<>();
		if (showExtraUnmatched) {
			actions.add(new TxrAction() {
				@Override
				public String getLabel() {
					return "OK, don't expect another collect match here after all";
				}
				@Override
				public CommandId getId() {
					return CommandId.ExpectAnotherCollectMatch;
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
			});
		}
		callback.createDirective(txrLineNumber, startLineNumber, indentation, actions.toArray(new TxrAction[0]));

		for (MatcherResultSuccess successfulMatch : successfulMatches) {
			successfulMatch.createControls(callback, indentation + 1);
		}
		
		// Either the body failed to match or the @(until) failed to match

		if (failedMatch != null) {
			failedMatch.createControls(callback, indentation + 1);
		}
		if (untilMatch != null) {
			TxrAction action = new TxrAction() {
				@Override
				public String getLabel() {
					return "Actually, it's okay that this @(until) does not match anything";
				}
				@Override
				public CommandId getId() {
					return new CommandId.ExpectUntilToMatchGivenLine(-1);
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

			// It's possible there is an @(until) clause that succeeded (if, for example,
			// the user is forcing an extra body match).
			// We only want to show the @(until) clause if it failed and the user is forcing
			// it to match.
			if (!untilMatch.isSuccess()) {
				callback.createDirective(untilMatch.getFailedResult().txrLineNumber, untilMatch.getFailedResult().startLineNumber, indentation, new TxrAction[]{ action });
				untilMatch.createControls(callback, indentation + 1);
			}
		}
	}

}

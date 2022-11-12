package txr.matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import txr.matchers.MatcherResult.CommandId;
import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;
import txr.matchers.TxrState.LineState;

public class MatcherResultSkipFailure extends MatcherResultFailed {

	private List<MatcherResultFailed> attempts;

	private MatchContext context;

	private LineState stateOfThisLine;
	
	public MatcherResultSkipFailure(int txrLineNumber, int startLineNumber, List<MatcherResultFailed> attempts, MatchContext context, LineState stateOfThisLine) {
		super(txrLineNumber, startLineNumber);
		this.attempts = attempts;
		this.context = context;
		this.stateOfThisLine = stateOfThisLine;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		// For now, take the best 10.  We could be cleverer, for example if there were 11 with the same
		// good score and the rest with the same worse score, then returning 11 would make sense.
		
		Collections.sort(attempts, new Comparator<MatcherResultFailed>() {
			@Override
			public int compare(MatcherResultFailed attemptA, MatcherResultFailed attemptB) {
				return attemptB.getScore() - attemptA.getScore();
			}
		});
		
		MatcherResultFailed bestFailedMatch = attempts.get(0);
		
		int showSkippingToThisLine = stateOfThisLine == null ? -1 : stateOfThisLine.showSkippingToThisLine;

		assert showSkippingToThisLine == -1; // this would be an 'exception' instance, not a 'success' instance, if showSkippingToThisLine set

		List<TxrAction> actions = new ArrayList<>();
		for (MatcherResultFailed attempt : attempts) {
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
				@Override
				public int getTxrLineNumber() {
					return txrLineNumber;
				}
				@Override
				public int getDataLineNumber() {
					return startLineNumber;
				}
			};
			actions.add(action);
			if (actions.size() >= 10) break;
		}
	
		callback.createDirective(txrLineNumber, startLineNumber, indentation, actions.toArray(new TxrAction[0]));
		
		bestFailedMatch.createControls(callback, indentation);
//		callback.createMismatch(txrLineNumber, startLineNumber, indentation, "All possible skips failed to match");
	}

}

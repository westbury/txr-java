package txr.matchers;

import txr.matchers.TxrState.LineState;

public class MatcherResult {

	private MatcherResultSuccess success;
	
	private MatcherResultFailed failed;
	
	public MatcherResult(MatcherResultSuccess result) {
		this.success = result;
	}
	
	public MatcherResult(MatcherResultFailed result) {
		this.failed = result;
	}
	
	public MatcherResultSuccess getSuccessfulResult() {
		return success;
	}
	
	public MatcherResultFailed getFailedResult() {
		return failed;
	}

	public boolean isSuccess() {
		return success != null;
	}
	
	public interface TxrAction {
		CommandId getId();
		String getLabel();
		boolean isClearingCommand();
	}
	
	public static abstract class CommandId {
		static CommandId ExpectAnotherCollectMatch = new CommandId() {
			@Override
			public void execute(LineState lineState, boolean isClearingCommand) {
				lineState.showExtraUnmatched = !isClearingCommand;
			}
		};
		static CommandId ExpectOptionalToMatch = new CommandId() {
			@Override
			public void execute(LineState lineState, boolean isClearingCommand) {
				lineState.showFailingMaybe = !isClearingCommand;
			}
		};
		static CommandId ExpectNoneClauseToFail = new CommandId() {
			@Override
			public void execute(LineState lineState, boolean isClearingCommand) {
				lineState.showAllFailuresInNone = !isClearingCommand;
			}
		};

		public static class ExpectSkipToGivenLine extends CommandId {
			public final int skippedToLineNumber;
			public ExpectSkipToGivenLine(int skippedToLineNumber) {
				this.skippedToLineNumber = skippedToLineNumber;
			}
			@Override
			public void execute(LineState lineState, boolean isClearingCommand) {
				if (isClearingCommand) {
					lineState.showSkippingToThisLine = -1;
				} else {
					lineState.showSkippingToThisLine = skippedToLineNumber;
				}
			}
		}

		public abstract void execute(LineState lineState, boolean isClearingCommand);
	}
	
	public interface TxrCommandExecution {
		CommandId getCommandId();
		int getTxrLineNumber();
		int getDataLineNumber();
		boolean isClearingCommand();
		void execute(LineState lineState);
	}
	
	public interface IControlCallback {
		
		void createDirective(int txrLineIndex, int textDataLineNumber, int indentation, TxrAction[] actions);
		
		void createMatch(int txrLineNumber, int textDataLineNumber, int indentation);

		void createMismatch(int txrLineNumber, int textDataLineNumber, int indentation, String message);

		/**
		 * Sometimes the same lines of input data can be matched multiple times. Examples are
		 * the @(until) clause of a @(collect) which does not consume the data, or cases in a
		 * @(maybe) where multiple can match.
		 * <P>
		 * To support this, the input data can be wound back to a previous line.
		 */
		void rewind(int textDataLineNumber);

		/**
		 * Currently, this is called only for an @(assert) that failed.
		 */
		void createDirectiveWithError(int txrLineNumber, int startLine, int indentation);
		
		/**
		 * Final call. This is made only if there is no match (or if user forced a condition that results
		 * in no match)
		 */
		void showRemainingLines();
	}
	
	public void createControls(IControlCallback callback) {
		this.createControls(callback, 0);
		if (failed != null) {
			callback.showRemainingLines();
		}
	}

	public void createControls(IControlCallback callback, int indentation) {
		if (success != null) {
			success.createControls(callback, indentation);
		}
		if (failed != null) {
			failed.createControls(callback, indentation);
		}
	}
	
	public String toString() {
		if (success != null && failed == null) {
			return "Success: " + success.toString();
		}
		if (success == null && failed != null) {
			return "Failed: " + failed.toString();
		}
		return "bad";
	}
}

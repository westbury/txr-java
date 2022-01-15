package txr.matchers;

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
	
	public interface IControlCallback {
		
		void createDirective(int txrLineIndex, int textDataLineNumber, int indentation);
		
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
	}
	
	public void createControls(IControlCallback callback) {
		this.createControls(callback, 0);
	}

	public void createControls(IControlCallback callback, int indentation) {
		if (success != null) {
			success.createControls(callback, indentation);
		}
		if (failed != null) {
			failed.createControls(callback, indentation);
		}
	}
}

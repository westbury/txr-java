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
}

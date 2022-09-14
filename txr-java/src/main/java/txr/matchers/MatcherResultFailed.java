package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public abstract class MatcherResultFailed {

	protected int score = 0;
	
	public abstract void createControls(IControlCallback callback, int indentation);

	public boolean isException() {
		return false;
	}

	/**
	 * Although the matcher failed, we want to have an indication of how far
	 * the matcher succeeded.  Did the matcher fail on the first line or did it
	 * fail way down.
	 * 
	 * @return a number, I don't know what this number is exactly yet, but the bigger it
	 * 		is, the more that matched
	 */
	public int getScore() {
		return score;
	}

}

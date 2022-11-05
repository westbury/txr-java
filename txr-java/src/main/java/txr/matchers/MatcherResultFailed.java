package txr.matchers;

import txr.matchers.MatcherResult.IControlCallback;

public abstract class MatcherResultFailed {

	protected final int txrLineNumber;
	
	protected final int startLineNumber;

	protected int score = 0;
	
	protected MatcherResultFailed(int txrLineNumber, int startLineNumber) {
		this.txrLineNumber = txrLineNumber;
		this.startLineNumber = startLineNumber;
	}
	
	public abstract void createControls(IControlCallback callback, int indentation);

	public boolean isException() {
		return false;
	}

	/**
	 * Although the matcher failed, we want to have an indication of how far
	 * the matcher succeeded.  Did the matcher fail on the first line or did it
	 * fail way down?
	 * 
	 * @return a number, I don't know what this number is exactly yet, but the bigger it
	 * 		is, the more that matched
	 */
	public int getScore() {
		return score;
	}

	/**
	 * We require access to this so we can put the line number in the action for, for example,
	 * @(skip)
	 * 
	 * @return
	 */
	public int getLineNumber() {
		return this.startLineNumber;
	}

}

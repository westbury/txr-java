package txr.matchers;

public class CallMatch {

	public final int txrLineNumber;

	public final int textDataLineNumber;

	public final int indentation;

	public CallMatch(int txrLineNumber, int textDataLineNumber, int indentation) {
		this.txrLineNumber = txrLineNumber;
		this.textDataLineNumber = textDataLineNumber;
		this.indentation = indentation;
	}

}

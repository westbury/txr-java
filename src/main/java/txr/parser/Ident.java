package txr.parser;

public class Ident extends Node {

	public final String id;
	
	private boolean longMatch;

	/**
	 * non-null only if a regular expression was specified
	 * for a bident
	 */
	public RegularExpression regex = null;
	
	public Ident(String id) {
		this.id = id;
	}

	@Override
	public boolean isNegativeMatcher() {
		return regex == null;
	}

	public void setLongMatch(boolean longMatch) {
		this.longMatch = longMatch;
	}

	public void setRegex(RegularExpression regex) {
		this.regex = regex;
	}

	public String toString() {
		if (longMatch) {
			return "Ident (long match): " + id;
		} else {
			return "Ident: " + id;
		}
	}
}

package txr.parser;

public class Ident extends Node {

	public final String id;
	
	private boolean longMatch;
	
	public Ident(String id) {
		this.id = id;
	}

	public void setLongMatch(boolean longMatch) {
		this.longMatch = longMatch;
	}

	public String toString() {
		if (longMatch) {
			return "Ident (long match): " + id;
		} else {
			return "Ident: " + id;
		}
	}
}

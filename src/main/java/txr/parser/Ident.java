package txr.parser;

public class Ident extends Node {

	private String id;
	
	private boolean longMatch;
	
	public Ident(String id) {
		this.id = id;
	}

	public void setLongMatch(boolean longMatch) {
		this.longMatch = longMatch;
	}

}

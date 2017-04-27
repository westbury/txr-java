package txr.matchers;

/**
 * This is something to which text is bound.
 * @author Nigel
 *
 */
public class Variable {

	public final String id;
	
	public Variable(String id) {
		this.id = id;
	}

	public String text = null;

	@Override
	public String toString() {
		return "@" + id + "=" + text;
	}
}

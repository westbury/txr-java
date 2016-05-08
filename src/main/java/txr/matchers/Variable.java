package txr.matchers;

/**
 * This is something to which text is bound.
 * @author Nigel
 *
 */
public class Variable extends HorizontalMatcher {

	public final String id;
	
	public Variable(String id) {
		this.id = id;
	}

	public String text = null;

	@Override
	public boolean isNegativeMatcher() {
		// Negative matcher is no text bound
		return text == null;
	}
	@Override
	public boolean match(CharsFromInputLineReader reader) {
		if (text == null) {
			/* Not bound, so probably this method should not have
			been called.
			*/
			throw new RuntimeException("Interal error - should not have been called.");
		}

		int start = reader.getCurrent();
		
		int i = 0;
		while (i < text.length()) {
			if (text.charAt(i) != reader.fetchChar()) {
				reader.setCurrent(start);
				return false;
			}
		}
		
		return true;
	}
	
	
}

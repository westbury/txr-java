package txr.matchers;

import txr.parser.Ident;

/**
 * This class wraps a bound variable and all the following positive matchers
 * to make a composite matcher that is a positive matcher.  Note that all
 * HorizontalMatcher implementations are positive matchers with no backtracking.
 * 
 * @author Nigel
 *
 */
public class VariableMatcher extends HorizontalMatcher {

	private Ident variableNode;
	
	private HorizontalMatcher followingMatcher;

	public VariableMatcher(Ident variableNode, HorizontalMatcher followingMatcher) {
		this.variableNode = variableNode;
		this.followingMatcher = followingMatcher;
	}

	@Override
	public boolean match(CharsFromInputLineReader reader, MatchResults bindings) {
		Variable var = bindings.getVariable(variableNode.id);
		if (var.text == null) {
			int start = reader.getCurrent();
			
			// For each position, see we can match following text components
			do {
				int j = reader.getCurrent();

				if (followingMatcher.match(reader, bindings)) {
					// We're done.  We have a match

					// j points to the offset of the first bit of
					// following text, to variable to bound to text
					// up to j.
					var.text = reader.substring(start, j);
					return true;
				}

				if (reader.isEndOfLine()) {
					break;
				}
				
				reader.fetchChar();
			} while (true);
			
			return false;
		} else {

		int start = reader.getCurrent();
		
		int i = 0;
		while (i < var.text.length()) {
			if (var.text.charAt(i) != reader.fetchChar()) {
				reader.setCurrent(start);
				return false;
			}
			i++;
		}
		
		return true;
		}
	}

	public String toString() {
		return "{Variable: " + variableNode + ", Following: " + followingMatcher + "}";
	}
}

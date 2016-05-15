package txr.matchers;

import java.util.List;

/**
 * A matcher that is a sequence of sub-matchers.  This matcher will
 * match at the current position if the first sub-matcher matches at
 * that position, then the following sub-matcher matches at the
 * immediately following position and so on.
 * 
 * @author Nigel
 *
 */
public class TextSequenceMatcher extends HorizontalMatcher {

	
	private List<HorizontalMatcher> matchers;

	public TextSequenceMatcher(List<HorizontalMatcher> matchers) {
		this.matchers = matchers;
	}

	@Override
	public boolean match(CharsFromInputLineReader reader, MatchResults bindings) {
		int start = reader.getCurrent();
		
		int i = 0;
		do {
			HorizontalMatcher matcher = matchers.get(i);

				// It's not a negative matcher
				if (!matcher.match(reader, bindings)) {
					// Line cannot match
					reader.setCurrent(start);
					return false;
				}
			
			i++;
		} while (i < matchers.size());

		return true;
	}

	public String toString() {
		return matchers.toString();
	}
}

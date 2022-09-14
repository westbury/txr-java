package txr.matchers;

/**
 * This matcher matches only if positioned at the end of the line.
 * It does not 'advance' the character position in any way, so this
 * method can be called multiple times when positioned at the end of
 * a line and it will match every time.
 * 
 * @author Nigel
 *
 */
public class EndOfLineMatcher extends HorizontalMatcher {

	@Override
	public boolean match(CharsFromInputLineReader reader, MatchResults bindings) {
		return reader.isEndOfLine();
	}

	@Override
	public String toString() {
		return "EOL";
	}
}

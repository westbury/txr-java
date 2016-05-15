package txr.matchers;


/**
 * Something that matches.  E.g. some text, a collect group etc
 * 
 * @author Nigel
 *
 */
public abstract class Matcher {

	/**
	 * 
	 * @param reader positioned at the line to start matching,
	 * 			on exit from this method, positioned on the line following
	 * 			a match if matched, or position unchanged if no match
	 * @param bindings bindings are added here if a match, otherwise
	 * 			if no match this object is not altered
	 * @return true if match, false if no match starting at this line
	 */
	public abstract boolean match(LinesFromInputReader reader, MatchResults bindings);

}

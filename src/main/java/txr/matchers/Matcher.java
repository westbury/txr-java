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
	 * @return if it matched, an object that showed how it matched,
	 * 		if it did not match, an object that indicates the most successful
	 * 		paths
	 */
	public abstract MatcherResult match(LinesFromInputReader reader, MatchContext context);

}

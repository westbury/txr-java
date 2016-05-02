package txr.semantics;

/**
 * Base class for any matcher that involves multiple lines, i.e.
 * is a vertical directive.
 * 
 * @author Nigel
 *
 */
public abstract class VerticalMatcher extends Matcher {

	public abstract void addNextMatcherInMatchSequence(Matcher matcher);

}

package txr.semantics;

import txr.parser.Line;

/**
 * Something that matches.  E.g. some text, a collect group etc
 * 
 * @author Nigel
 *
 */
public abstract class Matcher {

	public abstract void match(DocumentMatch documentMatch);

}

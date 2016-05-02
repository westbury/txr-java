package txr.semantics;

import txr.parser.Line;

/**
 * A matcher that matches a given line from the TXR file.
 * 
 * The given line must be something that can be matched to a line
 * of input.  For example the line cannot contain a vertical directive
 * or, for example, be an @(end) line.
 * 
 * @author Nigel
 *
 */
public class LineMatcher extends Matcher {

	public LineMatcher(Line line) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void match(DocumentMatch documentMatch) {
		// TODO Auto-generated method stub

	}

}

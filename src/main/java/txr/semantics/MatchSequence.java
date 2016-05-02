package txr.semantics;

import java.util.ArrayList;
import java.util.List;

/**
 * This matcher simply has a sequence of matchers and it matches
 * by matching each matcher in turn.  It is the matcher that is
 * used at the top level.
 * 
 * @author Nigel
 *
 */
public class MatchSequence extends VerticalMatcher {

	List<Matcher> sequence = new ArrayList<>();
	
	@Override
	public void addNextMatcherInMatchSequence(Matcher matcher) {
		sequence.add(matcher);
	}

	@Override
	public void match(DocumentMatch documentMatch) {
		// TODO Auto-generated method stub
		
	}

	public String toString() {
		return sequence.toString();
	}
}

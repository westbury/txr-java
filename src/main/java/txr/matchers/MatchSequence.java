package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.parser.Expr;
import txr.parser.Symbol;

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
	public void addNextDirective(Expr directive) {
		/*
		 * Directives such as @(COLLECT) are processed at the top level, and
		 * directives specific to only certain blocks, such as @(UNTIL), are not
		 * applicable here.
		 */
		throw new RuntimeException("Unknown directive or unexpected at this location: @(" + ((Symbol)directive.subExpressions.get(0)).symbolText + ")" );
	}

	@Override
	public boolean match(LinesFromInputReader reader, MatchResults bindings) {
		MatchResults bindingsToAdd = new MatchResults();
		
		int start = reader.getCurrent();
		
		for (Matcher matcher : sequence) {
			boolean matches = matcher.match(reader, bindingsToAdd);
			if (!matches) {
				reader.setCurrent(start);
				return false;
			}
		}
		
		bindings.addAll(bindingsToAdd);
		return true;
	}

	public String toString() {
		return sequence.toString();
	}
}

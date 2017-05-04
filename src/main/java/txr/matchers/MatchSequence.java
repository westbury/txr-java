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
	public boolean match(LinesFromInputReader reader, MatchContext context) {
		int start = reader.getCurrent();
		
		/*
		 * Note that we should not be adding bindings if this match fails.
		 * Hence we need to create our sub-context.
		 */
		MatchResultsWithPending subBindings = new MatchResultsWithPending(context.bindings);
		MatchContext subContext = new MatchContext(subBindings, context.assertContext);
		
		for (Matcher matcher : sequence) {
			boolean matches = matcher.match(reader, subContext);
			if (!matches) {
				// This check needs to be done here so the user sees the actual line that failed.
				context.assertContext.checkMatchFailureIsOk(reader.getCurrent(), matcher);
				reader.setCurrent(start);
				return false;
			}
		}
		
		subBindings.commitPendingBindings();
		return true;
	}

	public String toString() {
		return sequence.toString();
	}
}

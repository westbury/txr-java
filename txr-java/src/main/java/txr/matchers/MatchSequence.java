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
	public void addNextDirective(int txrLineIndex, Expr directive) {
		/*
		 * Directives such as @(COLLECT) are processed at the top level, and
		 * directives specific to only certain blocks, such as @(UNTIL), are not
		 * applicable here.
		 */
		throw new RuntimeException("Unknown directive or unexpected at this location: @(" + ((Symbol)directive.subExpressions.get(0)).symbolText + ")" );
	}

	@Override
	public MatcherResult match(LinesFromInputReader reader, MatchContext context) {
		int start = reader.getCurrent();
		
		/*
		 * Note that we should not be adding bindings if this match fails.
		 * Hence we need to create our sub-context.
		 */
		MatchResultsWithPending subBindings = new MatchResultsWithPending(context.bindings);
		MatchContext subContext = new MatchContext(subBindings, context.state, context.assertContext);
		
		List<MatcherResultSuccess> successfulMatches = new ArrayList<>();
		
		for (Matcher matcher : sequence) {
			int line = reader.getCurrent();
			
			MatcherResult matches = matcher.match(reader, subContext);
			if (!matches.isSuccess()) {
				// This check needs to be done here so the user sees the actual line that failed.
				
				TxrAssertException failedAssert = context.assertContext.checkMatchFailureIsOk(reader.getCurrent(), matcher);
				if (failedAssert != null) {
					// This matcher throws an exception because we got a match failure after an @(assert)
					return new MatcherResult(new MatcherResultSequenceException(successfulMatches, matches.getFailedResult(), failedAssert));
				} else {
					context.assertContext.checkMatchFailureIsOk(reader.getCurrent(), matcher);
					reader.setCurrent(start);
					return new MatcherResult(new MatcherResultSequenceFailed(successfulMatches, matches.getFailedResult()));
				}

			}
			
			// It matches, so add this to our line matching list for debugging
			successfulMatches.add(matches.getSuccessfulResult());
		}
		
		subBindings.commitPendingBindings();
		return new MatcherResult(new MatcherResultSequenceSuccess(successfulMatches));
	}

	public String toString() {
		return sequence.toString();
	}

	@Override
	public void setTxrEndLineIndex(int txrLineIndex) {
		// TODO Auto-generated method stub
		// We don't need to know the end here?
	}
}

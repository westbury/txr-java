package txr.matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import txr.matchers.TxrState.LineState;
import txr.parser.Expr;
import txr.parser.IntegerLiteral;
import txr.parser.SubExpression;
import txr.parser.Symbol;

public class SkipMatcher extends VerticalMatcher {

	// Move to VerticalMatcher
	private int txrLineNumber;
	
	/**
	 * The maximum number of lines to check
	 */
	private Long count;
	
	/**
	 * The number of lines to skip before starting to test
	 */
	private Long start;

	private final MatchSequence content;


	public SkipMatcher(int txrLineNumber, Expr expr) {
		this.txrLineNumber = txrLineNumber;
		this.content = new MatchSequence(txrLineNumber);
		
		Iterator<SubExpression> iter = expr.subExpressions.iterator();
		
		// The first is the name of the directive, so ignore that.
		iter.next();
		
		if (iter.hasNext()) {
			SubExpression countExpression = iter.next();
			count = getIntegerFromExpression(countExpression);
			if (iter.hasNext()) {
				SubExpression startExpression = iter.next();
				start = getIntegerFromExpression(startExpression);
				if (iter.hasNext()) {
					throw new RuntimeException("@(skip) can have at most two parameters");
				}
			}
		}
	}

	private Long getIntegerFromExpression(SubExpression expression) {
		if (expression instanceof IntegerLiteral) {
			IntegerLiteral literal = (IntegerLiteral)expression;
			return literal.value;
		} else if (expression instanceof Symbol && ((Symbol)expression).symbolText.equals("nil")) {
			return null;
		} else {
			throw new RuntimeException("expression is not an integer");
		}
	}

	@Override
	public MatcherResult match(LinesFromInputReader reader, MatchContext context) {
		/*
		 * The context passed to us may require a match due to an @(assert).
		 * We don't pass on this context because the sub-matches below
		 * do not individually have to match.  We just have to find a match
		 * eventually.
		 */
		MatchContext subContext = new MatchContext(context.bindings, context.state);
		
		int startLine = reader.getCurrent();
		
		// Get state for this collect instance.
		LineState stateOfThisLine = subContext.getLineState(txrLineNumber, startLine);
		if (stateOfThisLine != null && stateOfThisLine.showSkippingToThisLine != -1) {
			// The user has stated that they expect the @(skip) to skip to the given line,
			// so that is what we show.
			int skippedToLine = stateOfThisLine.showSkippingToThisLine;
			reader.setCurrent(skippedToLine);
			
			MatcherResult matches = content.match(reader, subContext);
			if (matches.isSuccess()) {
				// Could potentially be a success if the user makes edits to make this match while
				// this skip-to line state is set.
				// TODO should we clear the state at this time? The user seems to have fixed the problem
				// and we need to consider the implication of leaving this state.
				return new MatcherResult(new MatcherResultSkipSuccess(this.txrLineNumber, startLine, skippedToLine, matches.getSuccessfulResult()));
			}

			MatcherResultFailed failedResult = matches.getFailedResult();
			// Because the state is forcing this skip-to line, we treat a regular mis-match
			// as an exception. So we do the same for both.
			return new MatcherResult(new MatcherResultSkipException(this.txrLineNumber, startLine, skippedToLine, failedResult, Collections.emptyList(), subContext, stateOfThisLine));
		} else {
			/*
			 * If the match fails, we compare the results to find the one that
			 * goes 'farthest' through the TXR input.
			 * 
			 * At some time we may implement a feature that allows the user to select the
			 * line that the @(skip) is expected to skip to, in case our guess is wrong.
			 */
			List<MatcherResultFailed> priorAttempts = new ArrayList<>();
			while (!reader.isEndOfFile()) {
				int skippedToLine = reader.getCurrent();
				MatcherResult matches = content.match(reader, subContext);
				if (matches.isSuccess()) {
					return new MatcherResult(new MatcherResultSkipSuccess(this.txrLineNumber, startLine, skippedToLine, matches.getSuccessfulResult()));
				}
	
				MatcherResultFailed failedResult = matches.getFailedResult();
				if (failedResult.isException()) {
					/*
					 * If an exception was thrown then we return results showing a skip to this line,
					 * even if skips to previous lines showed a better match.
					 * 
					 * This assumes that it is the @(assert) that needs fixing. It may be that the problem is that
					 * a previous line should have matched, meaning that the assert failure should never be reached.
					 * This is a less likely situation and the user can always force the skip to the previous line
					 * using the debugger.
					 */
					return new MatcherResult(new MatcherResultSkipException(this.txrLineNumber, startLine, skippedToLine, failedResult, priorAttempts, subContext, stateOfThisLine));
				}
				// Else it's a mismatch, but no exception
				priorAttempts.add(failedResult);
				
				// Move on to next 'skipped to' line
				reader.fetchLine();
			}
	
			return new MatcherResult(new MatcherResultSkipFailure(this.txrLineNumber, startLine, priorAttempts, subContext, stateOfThisLine));
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Skip[");
		sb.append(content.toString()).append("]");
		return sb.toString();
	}

	@Override
	public void addNextMatcherInMatchSequence(Matcher matcher) {
		content.addNextMatcherInMatchSequence(matcher);
	}

	@Override
	public void addNextDirective(int txrLineIndex, Expr directive) {
		// There are no special directives allowed after a @(skip)
		// that are not allowed in general.
		throw new RuntimeException("Unknown directive @(" + directive + ") or unexpected at this location.");
	}

	@Override
	public void setTxrEndLineIndex(int txrLineIndex) {
		// We don't need to know the end here???
	}
}

package txr.matchers;

import java.util.Iterator;

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

	private MatchSequence content = new MatchSequence();


	public SkipMatcher(int txrLineNumber, Expr expr) {
		this.txrLineNumber = txrLineNumber;
		
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
		
		/*
		 * If the match fails, we compare the results to find the one that
		 * goes 'furthest' through the TXR input.
		 * 
		 * At some time we may implement a feature that allows the user to select the
		 * line that the @(skip) is expected to skip to, in case our guess is wrong.
		 */
		int bestLine = -1;
		MatcherResultFailed best = null;
		while (!reader.isEndOfFile()) {
			int skippedToLine = reader.getCurrent();
			MatcherResult matches = content.match(reader, subContext);
			if (matches.isSuccess()) {
				return new MatcherResult(new MatcherResultSkipSuccess(this.txrLineNumber, startLine, skippedToLine, matches.getSuccessfulResult()));
			}
			if (matches.getFailedResult().isException()) {
				/*
				 * If an exception was thrown then we return results showing a skip to this line,
				 * even if skips to previous lines showed a better match.
				 * 
				 * This assumes that it is the @(assert) that needs fixing. It may be that the problem is that
				 * a previous line should have matched, meaning that the assert failure should never be reached.
				 * This is a less likely situation and the user can always force the skip to the previous line
				 * using the debugger.
				 */
				return new MatcherResult(new MatcherResultSkipFailure(this.txrLineNumber, startLine, skippedToLine, matches.getFailedResult()));
			}
			// Else it's a mismatch, but no exception
			int score = matches.getFailedResult().getScore();
			if (best == null || score > best.getScore()) {
				bestLine = reader.getCurrent(); 
				best = matches.getFailedResult();
			}
			
			// Move on to next 'skipped to' line
			reader.fetchLine();
		}

		return new MatcherResult(new MatcherResultSkipFailure(this.txrLineNumber, startLine, bestLine, best));
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

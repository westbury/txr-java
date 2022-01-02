package txr.matchers;

import java.util.Iterator;

import txr.parser.Expr;
import txr.parser.IntegerLiteral;
import txr.parser.SubExpression;
import txr.parser.Symbol;

public class SkipMatcher extends VerticalMatcher {

	/**
	 * The maximum number of lines to check
	 */
	private Long count;
	
	/**
	 * The number of lines to skip before starting to test
	 */
	private Long start;

	private MatchSequence content = new MatchSequence();
	

	public SkipMatcher(Expr expr) {
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
		MatchContext subContext = new MatchContext(context.bindings);
		
		MatcherResult matches = content.match(reader, subContext);
		while (!matches.isSuccess() && !reader.isEndOfFile()) {
			reader.fetchLine();
			matches = content.match(reader, subContext);
		}

		return matches;
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
	public void addNextDirective(Expr directive) {
		// There are no special directives allowed after a @(skip)
		// that are not allowed in general.
		throw new RuntimeException("Unknown directive @(" + directive + ") or unexpected at this location.");
	}
}

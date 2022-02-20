package txr.matchers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import txr.parser.Expr;
import txr.parser.SubExpression;
import txr.parser.Symbol;

public class AssertMatcher extends VerticalMatcher {

	private Symbol exceptionType = null;

	private List<SubExpression> parameters = new ArrayList<>();

	private int txrLineNumber;
	
	public AssertMatcher(int txrLineNumber, Expr expr) {
		this.txrLineNumber = txrLineNumber;
		
		Iterator<SubExpression> iter = expr.subExpressions.iterator();

		// The first is the name of the directive, so ignore that.
		iter.next();

		if (iter.hasNext()) {
			SubExpression exceptionType = iter.next();
			if (!(exceptionType instanceof Symbol)) {
				throw new RuntimeException("not a throwable exception type");
			}
			this.exceptionType = (Symbol)exceptionType;

			while (iter.hasNext()) {
				SubExpression parameter = iter.next();
				this.parameters.add(parameter);
			}
		}
	}

	@Override
	public void addNextMatcherInMatchSequence(Matcher matcher) {
		// TODO refactor so we don't need to implement this.
		throw new RuntimeException();
	}

	@Override
	public void addNextDirective(int txrLineIndex, Expr expr) {
		// TODO refactor so we don't need to implement this.
		throw new RuntimeException();
	}
	
	@Override
	public MatcherResult match(LinesFromInputReader reader, MatchContext context) {
		/*
		 * An 'assert' directive will always matches.  However we mark the bindings
		 * to indicate that it is an error if the bindings are to be discarded due
		 * to the lack of a match.
		 */
		MatcherResultAssert result = new MatcherResultAssert(txrLineNumber, reader.getCurrent());
		context.assertContext.setMatchObligatory(result);
		return new MatcherResult(result);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Assert");
		if (exceptionType != null) {
			sb.append(" exceptionType=").append(exceptionType.symbolText);
		}
		return sb.toString();
	}

	@Override
	public void setTxrEndLineIndex(int txrLineIndex) {
		// TODO Do we need this?
		
	}
}

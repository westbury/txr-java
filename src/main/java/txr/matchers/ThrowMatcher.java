package txr.matchers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import txr.parser.Expr;
import txr.parser.SubExpression;
import txr.parser.Symbol;

public class ThrowMatcher extends VerticalMatcher {

	private Symbol exceptionType = null;

	private List<SubExpression> parameters = new ArrayList<>();
	
	public ThrowMatcher(Expr expr) {
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
	public void addNextDirective(Expr expr) {
		// TODO refactor so we don't need to implement this.
		throw new RuntimeException();
	}
	
	@Override
	public boolean match(LinesFromInputReader reader, MatchContext context) {
		// TODO make this a checked exception and be sure
		// we are handling it correctly in all places.
		
		throw new TxrException(exceptionType.symbolText, parameters, reader.getCurrent());
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Throw");
		if (exceptionType != null) {
			sb.append(" exceptionType=").append(exceptionType.symbolText);
		}
		return sb.toString();
	}
}

package txr.matchers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import txr.parser.Expr;
import txr.parser.StringLiteral;
import txr.parser.SubExpression;
import txr.parser.Symbol;

public class BindMatcher extends VerticalMatcher {

	private SubExpression target;

	private SubExpression value;

	private List<SubExpression> parameters = new ArrayList<>();
	
	public BindMatcher(Expr expr) {
		Iterator<SubExpression> iter = expr.subExpressions.iterator();

		// The first is the name of the directive, so ignore that.
		iter.next();

		if (iter.hasNext()) {
			this.target = iter.next();
//			if (!(target instanceof Symbol)) {
//				throw new RuntimeException("not a throwable exception type");
//			}
			if (iter.hasNext()) {
				this.value = iter.next();
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
		// TODO implement this
		
		if (target instanceof Symbol && value instanceof StringLiteral) {
			Symbol t = (Symbol)target;
			StringLiteral v = (StringLiteral)value;
			
			context.bindings.getVariable(t.symbolText).text = v.value;
		}
		return new MatcherResult(new MatcherResultBind(reader.getCurrent()));
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Bind");
		sb.append(" target=").append(target);
		sb.append(" value=").append(value);
		return sb.toString();
	}

	@Override
	public void setTxrEndLineIndex(int txrLineIndex) {
		// TODO Do we need this?
		
	}
}

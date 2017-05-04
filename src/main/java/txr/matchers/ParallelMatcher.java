package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.parser.Expr;
import txr.parser.Symbol;

public abstract class ParallelMatcher extends VerticalMatcher {

	private MatchSequence where;
	
	List<MatchSequence> content = new ArrayList<>();
	
	public ParallelMatcher() {
		where = new MatchSequence();
		content.add(where);
	}

	@Override
	public void addNextMatcherInMatchSequence(Matcher matcher) {
		where.addNextMatcherInMatchSequence(matcher);
	}

	@Override
	public void addNextDirective(Expr expr) {
		Symbol symbol = (Symbol)expr.subExpressions.get(0);
		switch (symbol.symbolText.toLowerCase()) {
			case "or":
			case "and":
				where = new MatchSequence();
				content.add(where);
				break;
				
			default:
				throw new RuntimeException("Unknown directive " + symbol.symbolText + " or unexpected at this location.");
		}
	}
	
	protected abstract String getDirectiveName();
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getDirectiveName() + "[");
		String separator = "";
		for (MatchSequence eachMatchSequence : content) {
			sb.append(eachMatchSequence.toString()).append(separator);
			separator = ", ";
		}
		sb.append("]");
		return sb.toString();
	}
}

package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.parser.Expr;
import txr.parser.Symbol;

public abstract class ParallelMatcher extends VerticalMatcher {

	protected int txrLineNumber;

	private MatchSequence where;
	
	protected static class Pair {
		public final int txrLineIndex;
		public final MatchSequence sequence;
		
		public Pair(int txrLineIndex, MatchSequence sequence) {
			this.txrLineIndex = txrLineIndex;
			this.sequence = sequence;
		}
	}
	
	protected List<Pair> content = new ArrayList<>();
	
	public ParallelMatcher(int txrLineNumber) {
		this.txrLineNumber = txrLineNumber;
		
		where = new MatchSequence();
		content.add(new Pair(txrLineNumber, where));
	}

	@Override
	public void addNextMatcherInMatchSequence(Matcher matcher) {
		where.addNextMatcherInMatchSequence(matcher);
	}

	@Override
	public void addNextDirective(int txrLineIndex, Expr expr) {
		Symbol symbol = (Symbol)expr.subExpressions.get(0);
		switch (symbol.symbolText.toLowerCase()) {
			case "or":
			case "and":
				where = new MatchSequence();
				content.add(new Pair(txrLineIndex, where));
				break;
				
			default:
				throw new RuntimeException("Unknown directive @(" + symbol.symbolText + ") or unexpected at this location.");
		}
	}
	
	protected abstract String getDirectiveName();
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getDirectiveName() + "[");
		String separator = "";
		for (Pair eachMatchSequence : content) {
			sb.append(eachMatchSequence.sequence.toString()).append(separator);
			separator = ", ";
		}
		sb.append("]");
		return sb.toString();
	}
}

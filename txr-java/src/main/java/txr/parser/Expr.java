package txr.parser;

import java.util.List;

public class Expr extends Node {

	public final List<SubExpression> subExpressions;
	
	public Expr(List<SubExpression> subExpressions) {
		this.subExpressions = subExpressions;
	}

	@Override
	public boolean isNegativeMatcher() {
		return false;
	}

	public String toString() {
		return subExpressions.toString();
	}
}

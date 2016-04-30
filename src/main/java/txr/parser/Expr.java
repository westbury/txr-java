package txr.parser;

import java.util.List;

public class Expr extends Node {

	public final List<SubExpression> subExpressions;
	
	public Expr(List<SubExpression> subExpressions) {
		this.subExpressions = subExpressions;
	}

	public String toString() {
		return subExpressions.toString();
	}
}

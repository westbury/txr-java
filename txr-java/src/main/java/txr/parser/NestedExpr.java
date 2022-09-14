package txr.parser;

import java.util.List;

public class NestedExpr extends SubExpression {

	List<SubExpression> subExpressions;
	
	public NestedExpr(List<SubExpression> subExpressions) {
		this.subExpressions = subExpressions;
	}

	public String toString() {
		return "Sub-Expressions: " + subExpressions.size();
	}
}

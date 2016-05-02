package txr.semantics;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import txr.parser.AST;
import txr.parser.Expr;
import txr.parser.Line;
import txr.parser.SubExpression;
import txr.parser.Symbol;

public class DocumentMatcher {

	private MatchSequence topLevelMatcher = new MatchSequence();
	
	public DocumentMatcher(AST ast) {
		
		int lineIndex = 0;
		
		Stack<Matcher> processorStack = new Stack<>();
		VerticalMatcher processor = topLevelMatcher;
		processorStack.push(processor);
		
		Line line = ast.lineSequence.get(lineIndex);
		
		if (line.nodes.size() == 1 && line.nodes.get(0) instanceof Expr) {
			Expr expr = (Expr) line.nodes.get(0);
			if (expr.subExpressions.size() >= 1) {
				SubExpression subExpression1 = expr.subExpressions.get(0);
				if (subExpression1 instanceof Symbol) {
					Symbol symbol = (Symbol) subExpression1;
					 
					switch (symbol.symbolText) {
					case "collect":
						CollectMatcher collectMatcher = new CollectMatcher(expr);
						processor.addNextMatcherInMatchSequence(collectMatcher);
						processorStack.push(collectMatcher);
						break;
						
					default:
						// It is not something that starts a new level of
						// nested element, so just pass on to the processor
						// for the current level.
						Matcher matcher = new LineMatcher(line);
						processor.addNextMatcherInMatchSequence(matcher);	
						matcher = null;
					}
					
				}
			}
		}
	}
	
	public DocumentMatch process(String inputText) {
		DocumentMatch results = new DocumentMatch(topLevelMatcher, inputText);
		
		return results;
	}
	
	public String toString() {
		return topLevelMatcher.toString();
	}
}

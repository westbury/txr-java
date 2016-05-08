package txr.matchers;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import txr.parser.AST;
import txr.parser.Expr;
import txr.parser.Ident;
import txr.parser.Line;
import txr.parser.SubExpression;
import txr.parser.Symbol;

public class DocumentMatcher {

	private MatchSequence topLevelMatcher = new MatchSequence();

	public DocumentMatcher(AST ast) {

		int lineIndex = 0;

		Stack<VerticalMatcher> processorStack = new Stack<>();
		VerticalMatcher processor = topLevelMatcher;

		do {
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
							processorStack.push(processor);
							processor = collectMatcher;
							break;

						case "end":
							processor = processorStack.pop();
							break;

						default:
							/*
							 * It is not a general directive. Maybe it is a specific
							 * directive that can appear only inside the block for a
							 * vertical directive, e.g. @(UNTIL) which can appear
							 * only inside the lines for @(COLLECT). So pass on to
							 * the processor for the current level.
							 */
							processor.addNextDirective(expr);	
						}

					} else {
						throw new RuntimeException("List found where first element is not a symbol.  Not sure this is allowed???");
					}
				} else {
					throw new RuntimeException("Empty list found.  Not sure this is allowed???");
				}
			} else {
				// Not a vertical directive
				Matcher lineMatcher = new LineMatcher(this, line);
				processor.addNextMatcherInMatchSequence(lineMatcher);
			}

			lineIndex++;
		} while (lineIndex < ast.lineSequence.size());
	}

	public LinesFromInputReader process(String [] inputText) {
		LinesFromInputReader results = new LinesFromInputReader(topLevelMatcher, inputText);

		return results;
	}

	public String toString() {
		return topLevelMatcher.toString();
	}

	private Map<String, Variable> variables = new HashMap<>();
	
	public Variable getVariable(Ident node) {
		Variable var = variables.get(node.id);
		if (var == null) {
			var = new Variable(node.id);
			variables.put(node.id, var);
		}
		return null;
	}
}

package txr.matchers;

import txr.parser.Ident;
import txr.parser.RegularExpression;

/**
 * This class represents a bound variable with a regex pattern.
 * This is a positive matcher.
 * 
 * @author Nigel
 *
 */
public class VariableMatcherWithRegex extends HorizontalMatcher {

	private Ident variableNode;
	
	private RegularExpression regularExpression;

	public VariableMatcherWithRegex(Ident variableNode, RegularExpression regularExpression) {
		this.variableNode = variableNode;
		this.regularExpression = regularExpression;
	}

	@Override
	public boolean match(CharsFromInputLineReader reader, MatchResults bindings) {
		int start = reader.getCurrent();

		boolean regexMatched = regularExpression.match(reader);

		if (!regexMatched) {
			return false;
		}

		int j = reader.getCurrent();
		String matchedText = reader.substring(start, j);

		Variable var = bindings.getVariable(variableNode.id);
		if (var.text == null) {
			var.text = matchedText;
			return true;
		} else {
			// Variable was already bound.
			if (matchedText.equals(var.text)) {
				return true;
			} else {
				reader.setCurrent(start);
				return false;
			}
		}
	}

	public String toString() {
		return "{Variable: " + variableNode + ", Regex: " + regularExpression + "}";
	}
}

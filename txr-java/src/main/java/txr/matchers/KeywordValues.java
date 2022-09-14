package txr.matchers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BinaryOperator;

import txr.parser.Expr;
import txr.parser.IntegerLiteral;
import txr.parser.SubExpression;
import txr.parser.Symbol;

public class KeywordValues {

	Map<String, SubExpression> map = new HashMap<>();

	public KeywordValues(Expr expr) {
		Iterator<SubExpression> iter = expr.subExpressions.iterator();
		
		// The first is the name of the directive, so ignore that.
		iter.next();
		
		while (iter.hasNext()) {
			SubExpression keyword = iter.next();
			SubExpression value = iter.next();
			String keywordAsText = ((Symbol)keyword).symbolText;
			map.put(keywordAsText, value);
		}
	}

	public Long removeInteger(String keywordName) {
		SubExpression value = map.remove(keywordName);
		if (value == null) {
		return null;
		} else if (!(value instanceof IntegerLiteral)) {
			throw new RuntimeException("Keyword " + keywordName + " must be an integer literal.");
		} else {
			return ((IntegerLiteral)value).value;
		}
	}

	public void failIfUnusedKeywords() {
		if (!map.isEmpty()) {
			String keywordList = map.keySet().stream().reduce(new BinaryOperator<String>() {
				@Override
				public String apply(String arg0, String arg1) {
					return arg0 + ", " + arg1;
				}
			}).get();
			throw new RuntimeException("Unsupported keywords : " + keywordList);
		}
		
	}
	
	
}

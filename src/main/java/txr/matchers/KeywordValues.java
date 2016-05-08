package txr.matchers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import txr.parser.Expr;
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

	public Integer removeInteger(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public void failIfUnusedKeywords() {
		// TODO Auto-generated method stub
		
	}
	
	
}

package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.parser.Expr;

public class CasesMatcher extends ParallelMatcher {

	public CasesMatcher(Expr expr) {
		KeywordValues keywordValues = new KeywordValues(expr);
		// No keywords here
		keywordValues.failIfUnusedKeywords();
	}

	@Override
	protected String getDirectiveName() {
		return "Cases";
	}
	
	@Override
	public boolean match(LinesFromInputReader reader, MatchResults bindings) {
		List<MatchResults> nestedBindingsList = new ArrayList<>();

		for (MatchSequence eachMatchSequence : content) {
			MatchResults nestedBindings = new MatchResults();

			// Look for a match
			if (eachMatchSequence.match(reader, nestedBindings)) {
				nestedBindingsList.add(nestedBindings);
				bindings.addList("cases", nestedBindingsList);
				return true;
			}
		}
		
		return false;
	}

}

package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.parser.Expr;

public class MaybeMatcher extends ParallelMatcher {

	public MaybeMatcher(Expr expr) {
		KeywordValues keywordValues = new KeywordValues(expr);
		// No keywords here
		keywordValues.failIfUnusedKeywords();
	}

	@Override
	protected String getDirectiveName() {
		return "Maybe";
	}
	
	@Override
	public boolean match(LinesFromInputReader reader, MatchResults bindings) {
		List<MatchResults> nestedBindingsList = new ArrayList<>();

		int start = reader.getCurrent();
		int longest = start;
		
		for (MatchSequence eachMatchSequence : content) {
			MatchResults nestedBindings = new MatchResults();

			// Look for a match
			if (eachMatchSequence.match(reader, nestedBindings)) {
				nestedBindingsList.add(nestedBindings);
				bindings.addList("maybe", nestedBindingsList);
				
				int endOfThisMatch = reader.getCurrent();
				if (endOfThisMatch > longest) {
					longest = endOfThisMatch;
				}
				
				// Reset for next one
				reader.setCurrent(start);
			}
		}

		reader.setCurrent(longest);
		
		return true;
	}

}

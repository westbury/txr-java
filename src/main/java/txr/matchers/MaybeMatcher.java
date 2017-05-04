package txr.matchers;

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
	public boolean match(LinesFromInputReader reader, MatchContext context) {
		int start = reader.getCurrent();
		int longest = start;
		
		for (MatchSequence eachMatchSequence : content) {
			MatchContext subContext = new MatchContext(context.bindings);
			
			if (eachMatchSequence.match(reader, subContext)) {
				
				int endOfThisMatch = reader.getCurrent();
				if (endOfThisMatch > longest) {
					longest = endOfThisMatch;
				}
				
				// Reset for next one
				reader.setCurrent(start);
			} else {
				/*
				 * The sub-sequence did not match.  Check only that
				 * the matching did not get as far as processing any @(assert)
				 * directives inside the sub-sequence.  If an @(assert)
				 * directive was processed then the failure to match is an error.
				 */
				subContext.assertContext.checkMatchFailureIsOk(reader.getCurrent(), eachMatchSequence);
			}
		}

		reader.setCurrent(longest);
		
		return true;
	}

}

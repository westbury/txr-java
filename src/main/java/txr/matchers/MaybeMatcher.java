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
	public MatcherResult match(LinesFromInputReader reader, MatchContext context) {
		int start = reader.getCurrent();
		int longest = start;
		List<MatcherResult> allMatcherResults = new ArrayList<>();
		
		for (MatchSequence eachMatchSequence : content) {
			MatchContext subContext = new MatchContext(context.bindings);
			
			MatcherResult eachMatcherResult = eachMatchSequence.match(reader, subContext);
			if (eachMatcherResult.isSuccess()) {
				
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
			
			allMatcherResults.add(eachMatcherResult);
		}

		reader.setCurrent(longest);
		
		return new MatcherResult(new MatcherResultMaybe(reader.getCurrent(), allMatcherResults));
	}

}

package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.parser.Expr;
import txr.parser.Symbol;

public class CollectMatcher extends VerticalMatcher {

	Long mintimes;
	Long maxtimes;
	Long lines;
	Long maxgap;
	Long mingap;

	enum BlockType {
		BODY,
		UNTIL,
		LAST
	};
	BlockType where = BlockType.BODY;
	
	MatchSequence body = new MatchSequence();
	
	MatchSequence until;
	
	MatchSequence last;
	
	public CollectMatcher(Expr expr) {
		KeywordValues keywordValues = new KeywordValues(expr);
		
		mintimes = keywordValues.removeInteger(":mintimes");
		maxtimes = keywordValues.removeInteger(":maxtimes");
		lines = keywordValues.removeInteger(":lines");
		maxgap = keywordValues.removeInteger(":maxgap");
		mingap = keywordValues.removeInteger(":mingap");
		
		Long gap = keywordValues.removeInteger(":gap");
		if (gap != null) {
			if (mingap != null || maxgap != null) {
				throw new RuntimeException("You cannot specify :gap if you have also specified either or both :mingap and :maxgap");
			}
			mingap = gap;
			maxgap = gap;
		}

		Long times = keywordValues.removeInteger(":times");
		if (times != null) {
			if (mintimes != null || maxtimes != null) {
				throw new RuntimeException("You cannot specify :times if you have also specified either or both :mintimes and :maxtimes");
			}
			mintimes = times;
			maxtimes = times;
		}

		keywordValues.failIfUnusedKeywords();
	}

	@Override
	public void addNextMatcherInMatchSequence(Matcher matcher) {
		switch (where) {
		case BODY:
			body.addNextMatcherInMatchSequence(matcher);
			break;
		case UNTIL:
			until.addNextMatcherInMatchSequence(matcher);
			break;
		case LAST:
			last.addNextMatcherInMatchSequence(matcher);
			break;
		}
	}

	@Override
	public void addNextDirective(Expr expr) {
		Symbol symbol = (Symbol)expr.subExpressions.get(0);
		switch (symbol.symbolText.toLowerCase()) {
			case "until":
				if (where != BlockType.BODY) {
					throw new RuntimeException("Can't have UNTIL directive if already in an UNTIL or LAST block in the same COLLECT.");
				}
				where = BlockType.UNTIL;
				until = new MatchSequence();
				break;
				
			case "last":
				if (where != BlockType.BODY) {
					throw new RuntimeException("Can't have LAST directive if already in an UNTIL or LAST block in the same COLLECT.");
				}
				where = BlockType.LAST;
				last = new MatchSequence();
				break;
			default:
				throw new RuntimeException("Unknown directive or unexpected at this location.");
		}
	}
	
	@Override
	public MatcherResult match(LinesFromInputReader reader, MatchContext context) {
		List<MatchResultsBase> nestedBindingsList = new ArrayList<>();

		List<MatcherResultSuccess> bodyMatchers = new ArrayList<>();
		MatcherResultSuccess untilMatch = null;
		MatcherResultSuccess lastMatch = null;

		int numberOfGapLines = 0;
		int endOfLastMatch = reader.getCurrent();
		do {
			int start = reader.getCurrent();
			if (until != null) {
				// Note that in this case the pending bindings are dropped,
				// even when the @(until) clause matches.
				MatchResultsWithPending nestedBindings = new MatchResultsWithPending(context.bindings);
				MatchContext nestedContext = new MatchContext(nestedBindings);
				
				String temp = reader.toString();
				MatcherResult untilMatcherResult = until.match(reader, nestedContext);
				if (untilMatcherResult.isSuccess()) {
					reader.setCurrent(start);
					untilMatch = untilMatcherResult.getSuccessfulResult();
					break;
				} else {
					/*
					 * The sub-sequence did not match.  Check only that
					 * the matching did not get as far as processing any @(assert)
					 * directives inside the sub-sequence.  If an @(assert)
					 * directive was processed then the failure to match is an error.
					 */
					// TODO this should not be needed because 'match' method should check.
					// The further 'in' the check, the better the error message.
					nestedContext.assertContext.checkMatchFailureIsOk(reader.getCurrent(), until);
				}
			}
		
			if (last != null) {
				MatchResultsWithPending nestedBindings = new MatchResultsWithPending(context.bindings);
				MatchContext nestedContext = new MatchContext(nestedBindings);
				
				MatcherResult lastMatcherResult = last.match(reader, nestedContext);
				if (lastMatcherResult.isSuccess()) {
					nestedBindingsList.add(nestedBindings.extractPendingAsBase());
					break;
				} else {
					/*
					 * The sub-sequence did not match.  Check only that
					 * the matching did not get as far as processing any @(assert)
					 * directives inside the sub-sequence.  If an @(assert)
					 * directive was processed then the failure to match is an error.
					 */
					// TODO this should not be needed because 'match' method should check.
					// The further 'in' the check, the better the error message.
					nestedContext.assertContext.checkMatchFailureIsOk(reader.getCurrent(), last);
				}
			}
			
			// Look for a match on the body
			MatchResultsWithPending nestedBindings = new MatchResultsWithPending(context.bindings);
			MatchContext nestedContext = new MatchContext(nestedBindings);
			
			MatcherResult bodyMatcherResult = body.match(reader, nestedContext);
			if (bodyMatcherResult.isSuccess()) {
				nestedBindingsList.add(nestedBindings.extractPendingAsBase());

				bodyMatchers.add(bodyMatcherResult.getSuccessfulResult());
				
				if (maxtimes != null && nestedBindingsList.size() == maxtimes) {
					break;
				}
				
				endOfLastMatch = reader.getCurrent();
				numberOfGapLines = 0;
			} else {
				/*
				 * The sub-sequence did not match.  Check only that
				 * the matching did not get as far as processing any @(assert)
				 * directives inside the sub-sequence.  If an @(assert)
				 * directive was processed then the failure to match is an error.
				 */
				// TODO this should not be needed because 'match' method should check.
				// The further 'in' the check, the better the error message.
				nestedContext.assertContext.checkMatchFailureIsOk(reader.getCurrent(), body);

				reader.fetchLine();
				numberOfGapLines++;
				
				if (maxgap != null && numberOfGapLines > maxgap) {
					/*
					 * If we stop collecting because no match was found within
					 * :maxgap lines then the input position is left at the end
					 * of the last successful match.  (This is how TXR works when
					 * :maxgap is zero - need to check behavior for non-zero maxgaps).
					 */
					reader.setCurrent(endOfLastMatch);
					break;
				}
			}
		} while (!reader.isEndOfFile());
		
		if (mintimes != null) {
			if (nestedBindingsList.size() < mintimes) {
				String message = "Collect has :mintimes set to " + mintimes + " but " + nestedBindingsList.size() + " matches were found.";
				return new MatcherResult(new MatcherResultCollectFailure(message, bodyMatchers, lastMatch, untilMatch));
			}
		}

		context.bindings.addList("collect", nestedBindingsList);
		return new MatcherResult(new MatcherResultCollectSuccess(bodyMatchers, lastMatch, untilMatch));
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Collect");
		if (mintimes != null) sb.append(" mintimes=" + mintimes);
		if (maxtimes != null) sb.append(" maxtimes=" + maxtimes);
		if (lines != null) sb.append(" lines=" + lines);
		if (maxgap != null) sb.append(" maxgap=" + maxgap);
		if (mingap != null) sb.append(" mingap=" + mingap);
		sb.append(" body=").append(body.toString());
		if (until != null) {
			sb.append(" until=").append(until.toString());
		}
		if (last != null) {
			sb.append(" last=").append(last.toString());
		}
		return sb.toString();
	}
}

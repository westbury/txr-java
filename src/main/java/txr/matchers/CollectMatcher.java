package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.parser.Expr;
import txr.parser.Symbol;

public class CollectMatcher extends VerticalMatcher {

	Integer mintimes;
	Integer maxtimes;
	Integer lines;
	Integer maxgap;
	Integer mingap;

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
		
		Integer gap = keywordValues.removeInteger(":gap");
		if (gap != null) {
			if (mingap != null || maxgap != null) {
				throw new RuntimeException("You cannot specify :gap if you have also specified either or both :mingap and :maxgap");
			}
			mingap = gap;
			maxgap = gap;
		}

		Integer times = keywordValues.removeInteger(":times");
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
	public boolean match(LinesFromInputReader reader, MatchContext context) {
		List<MatchResultsBase> nestedBindingsList = new ArrayList<>();
		do {
			int start = reader.getCurrent();
			if (until != null) {
				// Note that in this case the pending bindings are dropped,
				// even when the @(until) clause matches.
				MatchResultsWithPending nestedBindings = new MatchResultsWithPending(context.bindings);
				MatchContext nestedContext = new MatchContext(nestedBindings);
				
				String temp = reader.toString();
				if (until.match(reader, nestedContext)) {
					reader.setCurrent(start);
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
				
				if (last.match(reader, nestedContext)) {
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
			
			if (body.match(reader, nestedContext)) {
				nestedBindingsList.add(nestedBindings.extractPendingAsBase());
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
			}
		} while (!reader.isEndOfFile());
		
		context.bindings.addList("collect", nestedBindingsList);
		return true;
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

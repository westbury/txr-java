package txr.semantics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import txr.parser.Expr;

public class CollectMatcher extends VerticalMatcher {

	Integer mintimes;
	Integer maxtimes;
	Integer lines;
	Integer maxgap;
	Integer mingap;

	List<Matcher> body = new ArrayList<>();
	
	List<Matcher> until = new ArrayList<>();
	
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void match(DocumentMatch documentMatch) {
		// TODO Auto-generated method stub
		
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
		sb.append(" until=").append(until.toString());
		return sb.toString();
	}
}

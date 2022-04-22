package txr.matchers;

import java.util.List;

import txr.matchers.MatcherResult.IControlCallback;
import txr.matchers.MatcherResult.TxrAction;

public class MatcherResultMaybeSuccess extends MatcherResultSuccess {

	private int txrLineNumber;
	
	private int startLineNumber;
	
	private List<MatcherResult> subClauseResults;

	public MatcherResultMaybeSuccess(int txrLineNumber, int startLineNumber, List<MatcherResult> subClauseResults) {
		this.txrLineNumber = txrLineNumber;
		this.startLineNumber = startLineNumber;
		this.subClauseResults = subClauseResults;
	}

	@Override
	public void createControls(IControlCallback callback, int indentation) {
		callback.createDirective(txrLineNumber, startLineNumber, indentation, new TxrAction[0]);

		// To avoid clutter, don't show a mismatch if a sub-clause did not match
		
		for (MatcherResult subClauseResult : subClauseResults) {
			callback.rewind(startLineNumber); // only needed if multiple sub-clauses???
			if (subClauseResult.isSuccess()) {
				subClauseResult.createControls(callback, indentation + 1);
			}
		}
		
	}

}

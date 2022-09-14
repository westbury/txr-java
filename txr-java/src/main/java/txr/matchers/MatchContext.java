package txr.matchers;

import java.util.Arrays;
import java.util.Optional;

import txr.matchers.TxrState.LineState;

public class MatchContext {

	public final MatchResults bindings;
	
	public final AssertContext assertContext;

	public final TxrState state;
	
	public MatchContext(MatchResults bindings, TxrState state) {
		this.bindings = bindings;
		this.state = state;
		this.assertContext = new AssertContext();
	}
	
	public MatchContext(MatchResults bindings, TxrState state, AssertContext assertContext) {
		this.bindings = bindings;
		this.state = state;
		this.assertContext = assertContext;
	}

	public LineState getLineState(int txrLineNumber, int dataLineNumber) {
		if (state != null) {
			Optional<LineState> stateOfThisLine = Arrays.stream(state.lineStates).filter(x -> x.txrLineNumber == txrLineNumber && x.dataLineNumber == dataLineNumber).findAny();
			if (stateOfThisLine.isPresent()) {
				return stateOfThisLine.get();
			}
		}
		return null;
	}
	
}

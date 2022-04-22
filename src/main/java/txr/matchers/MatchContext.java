package txr.matchers;

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
	
}

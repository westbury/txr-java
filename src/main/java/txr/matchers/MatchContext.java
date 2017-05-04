package txr.matchers;

public class MatchContext {

	public final MatchResults bindings;
	
	public final AssertContext assertContext;
	
	public MatchContext(MatchResults bindings) {
		this.bindings = bindings;
		this.assertContext = new AssertContext();
	}
	
	public MatchContext(MatchResults bindings, AssertContext assertContext) {
		this.bindings = bindings;
		this.assertContext = assertContext;
	}
	
}

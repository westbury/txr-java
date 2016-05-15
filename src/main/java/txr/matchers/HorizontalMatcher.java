package txr.matchers;

public abstract class HorizontalMatcher {

	// Most matchers are not negative matchers.  Override when
	// necessary.
	public boolean isNegativeMatcher() {
		return false;
	};

	public abstract boolean match(CharsFromInputLineReader reader, MatchResults bindings);


}

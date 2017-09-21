package txr.parser;

import txr.matchers.CharsFromInputLineReader;

public class OptionalMatcher extends RegexMatcher {

	RegexMatcher childMatcher;
	
	public OptionalMatcher(RegexMatcher childMatcher) {
		this.childMatcher = childMatcher;
	}
 
	@Override
	public boolean match(CharsFromInputLineReader reader) {
		childMatcher.match(reader);
		
		// It does not matter if the above matched or not.
		// We match either way.
		return true;
	}

}

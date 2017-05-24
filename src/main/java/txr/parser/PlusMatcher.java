package txr.parser;

import txr.matchers.CharsFromInputLineReader;

public class PlusMatcher extends RegexMatcher {

	RegexMatcher childMatcher;
	
	public PlusMatcher(RegexMatcher childMatcher) {
		this.childMatcher = childMatcher;
	}
 
	@Override
	public boolean match(CharsFromInputLineReader reader) {
		boolean isMatch = childMatcher.match(reader);
		if (!isMatch) {
			return false;
		}
		
		do {
			isMatch = childMatcher.match(reader);
		} while (isMatch);
		
		return true;
	}

}

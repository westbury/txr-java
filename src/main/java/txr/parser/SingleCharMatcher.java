package txr.parser;

import txr.matchers.CharsFromInputLineReader;

public class SingleCharMatcher extends RegexMatcher {

	CharMatcher charMatcher;
	
	public SingleCharMatcher(CharMatcher charMatcher) {
		this.charMatcher = charMatcher;
	}

	@Override
	public boolean match(CharsFromInputLineReader reader) {
		int start = reader.getCurrent();
		char c = reader.fetchChar();
		if (!charMatcher.isMatch(c)) {
			reader.setCurrent(start);
			return false;
		}
		return true;
	}

}

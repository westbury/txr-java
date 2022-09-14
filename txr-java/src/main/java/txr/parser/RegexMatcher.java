package txr.parser;

import txr.matchers.CharsFromInputLineReader;

public abstract class RegexMatcher {

	public abstract boolean match(CharsFromInputLineReader reader);

}

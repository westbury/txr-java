package txr.parser;

import java.util.List;

import txr.matchers.CharsFromInputLineReader;

public class RegularExpression extends SubExpression {

	List<RegexMatcher> regexMatchers;
	
	public RegularExpression(List<RegexMatcher> regexMatchers) {
		this.regexMatchers = regexMatchers;
	}

	public boolean match(CharsFromInputLineReader reader) {
		int start = reader.getCurrent();

		for (RegexMatcher regexMatcher : regexMatchers) {
			if (!regexMatcher.match(reader)) {
				reader.setCurrent(start);
				return false;
			}
		}
		
		return true;
	}

}

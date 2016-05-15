package txr.matchers;

import java.util.Collections;
import java.util.List;

import txr.parser.TextNode;

public class TextMatcher extends HorizontalMatcher {

	List<String> text;
	
	public TextMatcher(TextNode node) {
		this.text = Collections.singletonList(node.text);
	}

	public TextMatcher(String text) {
		this.text = Collections.singletonList(text);
	}

	@Override
	public boolean match(CharsFromInputLineReader reader, MatchResults bindings) {
		/*
		 * Must match the text strings exactly. One or more spaces must exist in
		 * the input between each the elements in the text array.
		 */

		int start = reader.getCurrent();
		
		for (int i = 0; i < text.get(0).length(); i++) {
			if (reader.fetchChar() != text.get(0).charAt(i)) {
				return false;	
			}
		}

		for (int j = 1; j < text.size(); j++) {
			String word = text.get(j);

			if (reader.fetchChar() != ' ') {
				reader.setCurrent(start);
				return false;
			}

			char c = reader.fetchChar();
			while (c == ' ') {
				c = reader.fetchChar();
			}
			
			for (int i = 0; i< word.length(); i++) {
				if (reader.fetchChar() != word.charAt(i)) {
					return false;	
				}
			}
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer()
		.append("Text: [");
		String separator = "";
		for (String word : text) {
			buffer.append(separator).append('*').append(word).append('*');
			separator = ", ";
		}
		return buffer.append("]").toString();
	}
}

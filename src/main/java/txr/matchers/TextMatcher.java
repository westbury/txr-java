package txr.matchers;

import txr.parser.TextNode;

public class TextMatcher extends HorizontalMatcher {

	String [] text;
	
	public TextMatcher(TextNode node) {
		text = new String [] { node.text };
	}

	@Override
	public boolean match(CharsFromInputLineReader reader) {
		/*
		 * Must match the text strings exactly. One or more spaces must exist in
		 * the input between each the elements in the text array.
		 */

		int start = reader.getCurrent();
		
		for (int i = 0; i < text[0].length(); i++) {
			if (reader.fetchChar() != text[0].charAt(i)) {
				return false;	
			}
		}

		for (int j = 1; j < text.length; j++) {
			String word = text[j];

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

}

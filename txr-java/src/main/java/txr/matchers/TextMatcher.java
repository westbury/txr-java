package txr.matchers;

import java.util.ArrayList;
import java.util.List;

import txr.parser.TextNode;

public class TextMatcher extends HorizontalMatcher {

	List<String> text = new ArrayList<>();
	
	boolean tabsAlsoMatchSingleSpace;

	private boolean startWithWhitespace = false;

	private String endWithText = null;
	
	public TextMatcher(TextNode node, boolean tabsAlsoMatchSingleSpace) {
		// Split the text based on single spaces (non-whitespace character before the space
		// and non-whitespace character after the space).  These are the spaces that
		// match any number of spaces and tabs.
		
		int i = 0;
		
		if (node.text.charAt(0) == ' '
				&& (node.text.length() == 1 || !spaceOrTab(node.text.charAt(1)))) {
			this.startWithWhitespace = true;
			i = 1;
		}
		
		int start = i;
		while (i < node.text.length()) {
			while (i < node.text.length() && node.text.charAt(i) != ' ') {
				i++;
			}
			
			if (i == node.text.length()) {
				break;
			}
			
			if (i + 1 == node.text.length() || !spaceOrTab(node.text.charAt(i + 1))) {
				String thisWord = node.text.substring(start,  i);
				this.text.add(thisWord);
				i++;
				start = i;
				continue;
			}
			
			if (i == node.text.length()) {
				break;
			}
			
			// Whitespace but not a single space, so continue adding to current word
			while (i < node.text.length() && spaceOrTab(node.text.charAt(i))) {
				i++;
			}
		}
		
		if (start < i) {
			String thisWord = node.text.substring(start,  i);
			this.endWithText = thisWord;
		}

		this.tabsAlsoMatchSingleSpace = tabsAlsoMatchSingleSpace;
	}

	private boolean spaceOrTab(char c) {
		return c == ' ' || c == '\t';
	}
	
	@Override
	public boolean match(CharsFromInputLineReader reader, MatchResults bindings) {
		/*
		 * Must match the text strings exactly. One or more spaces must exist in
		 * the input between each the elements in the text array.
		 */

		int start = reader.getCurrent();
		
		if (this.startWithWhitespace) {
			if (!isMatchedBySingleSpace(reader.fetchChar())) {
				reader.setCurrent(start);
				return false;
			}

			char c = reader.fetchChar();
			while (isMatchedBySingleSpace(c)) {
				c = reader.fetchChar();
			}
			reader.setCurrent(reader.getCurrent() - 1);
		}
		
		for (String word : text) {
			for (int i = 0; i< word.length(); i++) {
				if (reader.fetchChar() != word.charAt(i)) {
					return false;	
				}
			}

			if (!isMatchedBySingleSpace(reader.fetchChar())) {
				reader.setCurrent(start);
				return false;
			}

			char c = reader.fetchChar();
			while (isMatchedBySingleSpace(c)) {
				c = reader.fetchChar();
			}
			reader.setCurrent(reader.getCurrent() - 1);
		}

		if (this.endWithText != null) {
			for (int i = 0; i < this.endWithText.length(); i++) {
				if (reader.fetchChar() != this.endWithText.charAt(i)) {
					return false;	
				}
			}
		}
		
		return true;
	}

	private boolean isMatchedBySingleSpace(char c) {
		if (tabsAlsoMatchSingleSpace) {
		return c == ' ' || c == '\t';
		} else {
		return c == ' ';
		}
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
		if (endWithText != null) {
			buffer.append(separator).append('*').append(endWithText).append('*');
		}
		return buffer.append("]").toString();
	}
}

package txr.matchers;

import java.util.List;

/**
 * The results of a successful match.
 *
 * @author Nigel
 *
 */
public class LinesFromInputReader {

	private String [] inputText;
	
	private int index = 0;

	public LinesFromInputReader(Matcher matcher, String [] inputText) {
		this.inputText = inputText;

		matcher.match(this);
		
	}

	public String fetchLine() {
		return inputText[index++];
	}

	public int getCurrent() {
		return index;
	}

	public void setCurrent(int index) {
		this.index = index;
		
	}

}

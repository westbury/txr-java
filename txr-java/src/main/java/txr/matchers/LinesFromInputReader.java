package txr.matchers;


/**
 * The results of a successful match.
 *
 * @author Nigel
 *
 */
public class LinesFromInputReader {

	private String [] inputText;
	
	private int index = 0;

	public LinesFromInputReader(String [] inputText) {
		this.inputText = inputText;
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

	public boolean isEndOfFile() {
		return index == inputText.length;
	}

	public String toString() {
		return "Reader at line " + (index+1) + ": " + inputText[index]; 
	}
}

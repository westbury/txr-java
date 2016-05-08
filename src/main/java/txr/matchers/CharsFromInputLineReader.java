package txr.matchers;


/**
 * The results of a successful match.
 *
 * @author Nigel
 *
 */
public class CharsFromInputLineReader {

	private String inputText;
	
	private int index = 0;

	public CharsFromInputLineReader(String inputText) {
		this.inputText = inputText;
	}

	public char fetchChar() {
		return inputText.charAt(index++);
	}

	public int getCurrent() {
		return index;
	}

	public void setCurrent(int index) {
		this.index = index;
		
	}

	public String substring(int i, int j) {
		return inputText.substring(i, j);
	}

	public boolean isEOL() {
		return index == inputText.length();
	}

}

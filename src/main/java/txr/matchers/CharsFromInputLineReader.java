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

	/**
	 * Fetches the character at the current cursor position and
	 * advances the cursor to the next character.
	 * <P>
	 * If the cursor is positioned at the end of the line then
	 * a newline character is returned.  This is a convenience behavior
	 * as it means code matching text known to contain no newline characters
	 * does not have to check for the end of the input; the newline character
	 * will result in a mismatch.
	 * 
	 * @return the character at which the cursor was positioned
	 * 			prior to the advance of the cursor
	 */
	public char fetchChar() {
		// As a convenience, we return a newline at the end.
		if (index == inputText.length()) {
			index++;
			return '\n';
		}
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

	public boolean isEndOfLine() {
		return index == inputText.length();
	}

}

package txr.semantics;

import java.util.List;

/**
 * The results of a successful match.
 *
 * @author Nigel
 *
 */
public class DocumentMatch {

	private String inputText;
	
	private int i = 0;

	public DocumentMatch(Matcher matcher, String inputText) {
		this.inputText = inputText;

		matcher.match(this);
		
	}

}

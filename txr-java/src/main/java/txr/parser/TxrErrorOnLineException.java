package txr.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * An exception that is thrown when an error occurs in the parser.
 * <P>
 * The parser will attempt to collect as many errors as possible.  This exception therefore
 * may contain details of more than one error.
 * 
 * @author Nigel
 *
 */
public class TxrErrorOnLineException extends Exception {
	private static final long serialVersionUID = 1L;

	public class ParseError {
		final int startColumn;
		final int endColumn;
		final String message;
		
		ParseError(int startColumn, int endColumn, String message) {
			this.startColumn = startColumn;
			this.endColumn = endColumn;
			this.message = message;
		}
	}
	List<ParseError> errors = new ArrayList<>();
	
	
	TxrErrorOnLineException(int startColumn, int endColumn, String message) {
		errors.add(new ParseError(startColumn, endColumn, message));
	}
	
	TxrErrorOnLineException(int column, String message) {
		errors.add(new ParseError(column, column+1, message));
	}
	
	@Override
	public String getMessage() {
		return "The parsing failed with " + errors.size() + " errors found.";
	}
}

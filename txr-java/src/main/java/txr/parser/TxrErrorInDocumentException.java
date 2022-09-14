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
public class TxrErrorInDocumentException extends Exception {
	private static final long serialVersionUID = 1L;

	public class ParseError {
		final int line;
		final int startColumn;
		final int endColumn;
		final String message;
		
		ParseError(int line, int startColumn, int endColumn, String message) {
			this.line = line;
			this.startColumn = startColumn;
			this.endColumn = endColumn;
			this.message = message;
		}
	}
	List<ParseError> errors = new ArrayList<>();
	
	
	TxrErrorInDocumentException(int line, int startColumn, int endColumn, String message) {
		errors.add(new ParseError(line, startColumn, endColumn, message));
	}
	
	TxrErrorInDocumentException(int line, int column, String message) {
		errors.add(new ParseError(line, column, column+1, message));
	}
	
	TxrErrorInDocumentException(int line, TxrErrorOnLineException exceptionWithoutLine) {
		for (TxrErrorOnLineException.ParseError error : exceptionWithoutLine.errors) {
			errors.add(new ParseError(line, error.startColumn, error.endColumn, error.message));
		}
	}
	
	/**
	 * Creates an exception that can be used to collect errors.  It should only be thrown
	 * if errors have been added to it.
	 */
	public TxrErrorInDocumentException() {
	}

	@Override
	public String getMessage() {
		return "The parsing failed with " + errors.size() + " errors found.";
	}

	/**
	 * If errors are thrown on a line, add those errors to the list of errors in\
	 * this document.
	 * 
	 * @param line
	 * @param exceptionWithoutLine
	 */
	public void add(int line, TxrErrorOnLineException exceptionWithoutLine) {
		for (TxrErrorOnLineException.ParseError error : exceptionWithoutLine.errors) {
			errors.add(new ParseError(line, error.startColumn, error.endColumn, error.message));
		}
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}
}

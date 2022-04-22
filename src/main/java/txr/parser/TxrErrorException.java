package txr.parser;

/**
 * An exception that is thrown when an error occurs in the parser.  This exception is thrown
 * when the thrower has no idea of the line or column or other context.  The exception must be
 * caught somewhere higher up the call stack and a replacement exception thrown that includes the
 * line and column.
 * <P>
 * The parser will attempt to collect as many errors as possible.  This exception therefore
 * may contain details of more than one error.
 * 
 * @author Nigel
 *
 */
public class TxrErrorException extends Exception {
	private static final long serialVersionUID = 1L;

	private String message;
	
	TxrErrorException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}

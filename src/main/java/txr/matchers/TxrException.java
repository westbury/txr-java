package txr.matchers;

import java.util.List;

import txr.parser.SubExpression;

public class TxrException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public final String exceptionType; 
	
	public final List<SubExpression> parameters;
	
	public final int lineNumber;
	
	public TxrException(String exceptionType, List<SubExpression> parameters, int lineIndex) {
		super("Throw " + exceptionType + " at line " + (lineIndex+1) + ", " + parameters);
		this.exceptionType = exceptionType;
		this.parameters = parameters;
		this.lineNumber = lineIndex + 1;
	}

}

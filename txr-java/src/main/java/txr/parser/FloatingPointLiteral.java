package txr.parser;

public class FloatingPointLiteral extends SubExpression {

	public final double value;
	
	public FloatingPointLiteral(String numberAsString) throws TxrErrorException {
		try {
			value = Double.valueOf(numberAsString);
		} catch (NumberFormatException e) {
			throw new TxrErrorException("'" + numberAsString + "' appears to be a floating-point number but it is out-of-range.");
		}
	}

	public String toString() {
		return "Float: " + value;
	}
}

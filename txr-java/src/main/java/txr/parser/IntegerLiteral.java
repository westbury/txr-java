package txr.parser;

public class IntegerLiteral extends SubExpression {

	public final long value;
	
	public IntegerLiteral(long value) {
		this.value = value;
	}

	public String toString() {
		return "Integer: " + value;
	}
}

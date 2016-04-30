package txr.parser;

public class IntegerLiteral extends SubExpression {

	public final int value;
	
	public IntegerLiteral(int value) {
		this.value = value;
	}

	public String toString() {
		return "Integer: " + value;
	}
}

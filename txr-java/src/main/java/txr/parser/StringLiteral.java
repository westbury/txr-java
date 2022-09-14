package txr.parser;

public class StringLiteral extends SubExpression {

	public final String value;
	
	public StringLiteral(String value) {
		this.value = value;
	}

	public String toString() {
		return "String: *" + value + "*";
	}
}

package txr.parser;

public class CharacterLiteral extends SubExpression {

	public final char value;
	
	public CharacterLiteral(char value) {
		this.value = value;
	}

	public String toString() {
		return "Char: " + ((int)value);
	}
}

package txr.parser;

public class Symbol extends SubExpression {

	public final String symbolText;
	
	public Symbol(String symbolText) {
		this.symbolText = symbolText;
	}

	public String toString() {
		return "Symbol: " + symbolText;
	}
}

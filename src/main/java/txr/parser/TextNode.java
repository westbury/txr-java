package txr.parser;

public class TextNode extends Node {

	public final String text;
	
	public TextNode(String text) {
		this.text = text;
	}

	public String toString() {
		return "Text: *" + text + "*";
	}
}

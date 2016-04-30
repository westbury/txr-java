package txr.parser;

import java.util.ArrayList;
import java.util.List;

public class AST {

	public final List<Line> lineSequence = new ArrayList<>();
	
	public void append(Line line) {
		lineSequence.add(line);
	}

	public String toString() {
		return lineSequence.toString();
	}

}

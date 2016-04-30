package txr.parser;

import java.util.ArrayList;
import java.util.List;

public class Line {

	List<Node> nodes = new ArrayList<>();
	
	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	public void append(Node node) {
		nodes.add(node);
	}

	public String toString() {
		return nodes.toString();
	}
}

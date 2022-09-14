package txr.matchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MatchResultsBase implements MatchResults {

	private Map<String, Variable> variables = new HashMap<>();
	
	private List<List<MatchResultsBase>> collections = new ArrayList<>();

	@Override
	public Variable getVariable(String variableId) {
		Variable var = variables.get(variableId);
		if (var == null) {
			var = new Variable(variableId);
			variables.put(variableId, var);
		}
		return var;
	}

	@Override
	public void addList(String key, List<MatchResultsBase> bindingsList) {
		collections.add(bindingsList);
	}

	@Override
	public List<MatchResultsBase> getCollections(int index) {
		return collections.get(index);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{\n");
		for (Entry<String, Variable> entry : variables.entrySet()) {
			final String value = entry.getValue().toString();
			sb.append("    " + value.replaceAll("\n", "\n    ") + "\n");
		}

		for (List<MatchResultsBase> entry2 : collections) {
			sb.append("    [\n");
			for (MatchResultsBase entry3 : entry2) {
				final String value = entry3.toString();
				sb.append("        " + value.replaceAll("\n", "\n        ") + "\n");
			}
			sb.append("    ]\n");
		}
		sb.append("}");
		
		return sb.toString();
	}

	@Override
	public boolean containsBinding(String variableId) {
		return variables.containsKey(variableId);
	}

}

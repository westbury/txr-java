package txr.matchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		return variables.toString() + collections.toString();
//		StringBuffer sb = new StringBuffer();
//		sb.append("{");
//		String separator = "";
//		for (Entry<String, Variable> entry : variables.entrySet()) {
//			sb.append(separator).append(entry.getKey()).append('=').append(entry.getValue());
//			separator=",";
//		}
//		sb.append("}");
//
//		separator = "";
//		Object entry;
//		for (entry : collections) {
//			sb.append(separator).append(entry.getKey()).append('=').append(entry.getValue());
//			separator=",";
//		}
//		sb.append("}");
	}

	@Override
	public boolean containsBinding(String variableId) {
		return variables.containsKey(variableId);
	}

}

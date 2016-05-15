package txr.matchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchResults {

	private Map<String, Variable> variables = new HashMap<>();
	
	private List<List<MatchResults>> collections = new ArrayList<>();
	
	public Variable getVariable(String variableId) {
		Variable var = variables.get(variableId);
		if (var == null) {
			var = new Variable(variableId);
			variables.put(variableId, var);
		}
		return var;
	}

	public void addAll(MatchResults bindingsToAdd) {
		// It's an internal error if entry already bound.
		// We could check just to be safe.
		variables.putAll(bindingsToAdd.variables);
		
		collections.addAll(bindingsToAdd.collections);
		
	}

	public void addList(String key, List<MatchResults> bindingsList) {
		collections.add(bindingsList);
	}

	public List<MatchResults> getCollections(int index) {
		return collections.get(index);
	}

	
}

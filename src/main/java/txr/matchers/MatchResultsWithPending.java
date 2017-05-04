package txr.matchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchResultsWithPending implements MatchResults {

	private final MatchResults parentBindings;
	
	private Map<String, Variable> variables = new HashMap<>();
	
	private List<List<MatchResultsBase>> collections = new ArrayList<>();

	public MatchResultsWithPending(MatchResults parentBindings) {
		this.parentBindings = parentBindings;
	}

	@Override
	public Variable getVariable(String variableId) {
		if (parentBindings.containsBinding(variableId)) {
			return parentBindings.getVariable(variableId);
		} else {
			Variable var = variables.get(variableId);
			if (var == null) {
				var = new Variable(variableId);
				variables.put(variableId, var);
			}
			return var;
		}
	}

	@Override
	public void addList(String key, List<MatchResultsBase> bindingsList) {
		collections.add(bindingsList);
	}

	@Override
	public List<MatchResultsBase> getCollections(int index) {
		// Should only be called on the base???
		throw new UnsupportedOperationException();
	}

	public String toString() {
		return variables.toString() + collections.toString();
	}

	@Override
	public boolean containsBinding(String variableId) {
		return variables.containsKey(variableId) || parentBindings.containsBinding(variableId);

	}

	public void commitPendingBindings() {
		for (Variable varFromChild : variables.values()) {
			assert !parentBindings.containsBinding(varFromChild.id);
			Variable varFromParent = parentBindings.getVariable(varFromChild.id);
			varFromParent.text = varFromChild.text;
		}
		
		for (List<MatchResultsBase> collect : collections) {
			parentBindings.addList(null, collect);
		}
	}

	/**
	 * Return just the pending bindings as a separate object.
	 * <P>
	 * This is used by the @(collect) when we need to move the pending
	 * bindings into the collection.
	 * 
	 * @return
	 */
	public MatchResultsBase extractPendingAsBase() {
		MatchResultsBase result = new MatchResultsBase();
		
		for (Variable varFromChild : variables.values()) {
			Variable varFromParent = result.getVariable(varFromChild.id);
			varFromParent.text = varFromChild.text;
		}
		
		for (List<MatchResultsBase> collect : collections) {
			result.addList(null, collect);
		}
		
		return result;
	}

}

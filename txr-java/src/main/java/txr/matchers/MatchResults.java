package txr.matchers;

import java.util.List;

public interface MatchResults {

	boolean containsBinding(String variableId);

	Variable getVariable(String variableId);

	void addList(String key, List<MatchResultsBase> bindingsList);
	
	List<MatchResultsBase> getCollections(int index);
}

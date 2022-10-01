package txr_java_testframework;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class TestCaseContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		return ((List<TxrTestCase>)inputElement).toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof TxrTestCase) {
			return ((TxrTestCase)parentElement).getInputDataTestCases().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		// Note we can't determine the parent of a TestCase object. The getTxrTestCase() method will
		// return the wrong TxrTestCase instance, being the original one that contained it and that provides
		// the correct base address for resolving relative paths. However the TestCase object may have been
		// merged to a different TxrTestCase instance.
		//
		// So just return null here, it makes no difference in this simple view.
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return element instanceof TxrTestCase;
	}

}

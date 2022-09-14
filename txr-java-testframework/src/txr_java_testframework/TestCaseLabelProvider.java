package txr_java_testframework;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class TestCaseLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	@Override
	public Image getImage(Object element) {
		if (element instanceof TestCase) {
			return ((TestCase) element).getImage();
		}
		throw new RuntimeException("error");
	}

	@Override
	public String getText(Object element) {
		if (element instanceof TestCase) {
			return ((TestCase) element).getLabel();
		}
		return "error";
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return getImage(element);
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		return getText(element);
	}

}

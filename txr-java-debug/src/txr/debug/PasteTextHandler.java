package txr.debug;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class PasteTextHandler {

	@Execute
	   public static void execute(Shell shell, MPart part) {
		try {
			Object txrDebugPart = part.getObject();
			((TxrDebugPart)txrDebugPart).pasteData();
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(shell, "Paste Failed", e.getMessage());
		}
	}
}

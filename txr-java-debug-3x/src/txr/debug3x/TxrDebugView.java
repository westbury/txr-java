package txr.debug3x;

import java.awt.Toolkit;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import txr.debug.ITxrSource;
import txr.debug.TxrDebugPart;


public class TxrDebugView extends ViewPart {

	public class PasteTxrAction extends Action {
		public PasteTxrAction() {
			super("Paste Txr");
			setToolTipText("Paste the TXR to be debugged");
			setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		}

		@Override
		public void run() {
			try {
				pojoView.pasteTxr();
			} catch (Exception e) {
				e.printStackTrace();
				MessageDialog.openError(getViewSite().getShell(), "Paste Failed", e.getMessage());
			}
		}
	}

	public class PasteTextAction extends Action {
		public PasteTextAction() {
			super("Paste Test Text");
			setToolTipText("Paste Test Text");
			setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		}

		@Override
		public void run() {
			try {
				pojoView.pasteData();
			} catch (Exception e) {
				e.printStackTrace();
				MessageDialog.openError(getViewSite().getShell(), "Paste Failed", e.getMessage());
			}
		}
	}

	public static String ID = "txr.debug.TxrDebugView";

	private TxrDebugPart pojoView;

	private PasteTxrAction pasteTxrAction;

	private PasteTextAction pasteTextAction;

	public TxrDebugView() {
		pasteTxrAction = new PasteTxrAction();
		pasteTextAction = new PasteTextAction();
	}

	@Override
	public void init(IViewSite viewSite, IMemento memento) throws PartInitException {
		super.init(viewSite, memento);

		//		if (memento != null) {
		//			filter.init(memento.getChild("filter"));
		//		}

	}

	@Override
	public void saveState(IMemento memento) {	
		super.saveState(memento);
		//		filter.saveState(memento.createChild("filter"));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		// No items are currently in the pull down menu.
		Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(new FlavorListener() { 
			@Override 
			public void flavorsChanged(FlavorEvent e) {

				System.out.println("ClipBoard UPDATED: " + e.getSource() + " " + e.toString());
			} 
		}); ;
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(pasteTxrAction);
		manager.add(pasteTextAction);
	}

	@Override
	public void createPartControl(Composite parent) {
		pojoView = new TxrDebugPart(parent);

		contributeToActionBars();
	}


	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		super.dispose();
//		errorImage.dispose();
	}

	// This is used for programmatic opening of view
	public void setTxrAndData(ITxrSource txrSource, String[] lines) {
		pojoView.setTxrAndData(txrSource, lines);
	}

}

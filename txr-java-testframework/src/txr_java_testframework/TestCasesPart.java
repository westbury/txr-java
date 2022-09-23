package txr_java_testframework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import txr.debug.TxrDebugPart;
import txr.matchers.DocumentMatcher;
import txr.matchers.DocumentMatcher.MatchPair;
import txr.matchers.MatcherResult.TxrCommandExecution;
import txr.matchers.TxrState;
import txr.parser.TxrErrorInDocumentException;

public class TestCasesPart {

	private Text txtInput;
	private TableViewer tableViewer;

	@Inject
	private MDirtyable dirty;

	@Inject
	private EPartService partService;

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		txtInput = createTextInput(parent);
		txtInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		tableViewer = createTableViewer(parent);
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

		Control buttonArea = createButtonArea(parent);
	}

	private Control createButtonArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));

		Button runButton = new Button(parent, SWT.PUSH);
		runButton.setText("Run");
		runButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MPart p = partService.showPart("txr-java-debug.debug-part", PartState.VISIBLE);
				TxrDebugPart debugPart = (TxrDebugPart)p.getObject();

				Object s = tableViewer.getStructuredSelection().getFirstElement();
				if (s instanceof TestCase) {
					TestCase testCase = (TestCase)s;
					try {
						debugPart.setTxrAndData(testCase.getTxrResource(), testCase.getInputData());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		return composite;
	}

	private TableViewer createTableViewer(Composite parent) {
		TableViewer tableViewer = new TableViewer(parent);

		tableViewer.setLabelProvider(new TestCaseLabelProvider());

		TestCase[] allTestCases = {
				new TestCase("collect - simple test", "collect - simple.txr", "collect - simple.txt"),
				new TestCase("collect - with a failing collect", "collect - simple.txr", "collect - one fails.txt")
		};

		for (TestCase testCase : allTestCases) {
			new Thread() {
				@Override
				public void run() {

					try (InputStream txrInputStream = testCase.getTxrResource().openStream()) {
						String[] testData = testCase.getInputData();
						DocumentMatcher matcher = new DocumentMatcher(txrInputStream, "UTF-8");

						TxrState state = null;
						TxrCommandExecution command = null;
						MatchPair results = matcher.process2(testData, state, command);

						testCase.setStatus(results.matcherResults.isSuccess() ? TestCase.Status.PASSED : TestCase.Status.FAILED);

						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (!tableViewer.getTable().isDisposed()) {
									tableViewer.refresh(testCase);
								}
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					} catch (TxrErrorInDocumentException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}.start();

			tableViewer.add(testCase);
		}

		return tableViewer;
	}

	private Text createTextInput(Composite parent) {
		Text txtInput = new Text(parent, SWT.BORDER);
		txtInput.setMessage("Enter text to mark part as dirty");
		txtInput.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dirty.setDirty(true);
			}
		});
		return txtInput;
	}

	@Focus
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	@Persist
	public void save() {
		dirty.setDirty(false);
	}

}
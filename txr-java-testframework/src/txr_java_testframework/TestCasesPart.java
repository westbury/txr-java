package txr_java_testframework;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import txr.debug.TxrDebugPart;
import txr.matchers.DocumentMatcher;
import txr.matchers.DocumentMatcher.MatchPair;
import txr.matchers.MatcherResult.TxrAction;
import txr.matchers.TxrState;
import txr.parser.TxrErrorInDocumentException;

public class TestCasesPart {

	private Text txtInput;
	private TreeViewer treeViewer;

	@Inject
	private MDirtyable dirty;

	@Inject
	private EPartService partService;

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		txtInput = createTextInput(parent);
		txtInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		treeViewer = createTableViewer(parent);
		treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

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

				Object s = treeViewer.getStructuredSelection().getFirstElement();
				if (s instanceof TestCase) {
					TestCase testCase = (TestCase)s;
					try {
						debugPart.setTxrAndData(testCase.getTxrTestCase().getTxrResource().toURI().toURL(), testCase.getInputData());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		return composite;
	}

	private TreeViewer createTableViewer(Composite parent) {
		TreeViewer tableViewer = new TreeViewer(parent);

		tableViewer.setLabelProvider(new TestCaseLabelProvider());
		tableViewer.setContentProvider(new TestCaseContentProvider());

		List<TxrTestCase> allTestCases = new ArrayList<>();

		Yaml yaml = new Yaml(new Constructor(TestConfiguration.class));

		ClassLoader classLoader = getClass().getClassLoader();
		URL yamlFileUrl = classLoader.getResource("tests.yaml");
		
		// If the resource is a bundleresource then this will copy the directory to the file system first.
		// This does mean that relative paths in this bundle's tests.yaml cannot go outside the root directory,
		// i.e. cannot start with '../'.
		try {
			URI yamlFileAsFileUri = FileLocator.toFileURL(yamlFileUrl).toURI();
			File yamlFile = new File(yamlFileAsFileUri);
			processYaml(yaml, yamlFile, allTestCases);
		} catch (URISyntaxException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		File home = new File(System.getProperty("user.home"));
		if (home != null && home.exists()) {
			// Mac does not like directory starting with '.'
			File userConfigFile = new File(home, "Documents/txrtester/tests.yaml");
			if (userConfigFile.exists()) {
				processYaml(yaml, userConfigFile, allTestCases);
			}
		}

		for (TxrTestCase txrTestCase : allTestCases) {
			new Thread() {
				@Override
				public void run() {
					try (InputStream txrInputStream = new FileInputStream(txrTestCase.getTxrResource())) {
						DocumentMatcher matcher = new DocumentMatcher(txrInputStream, "UTF-8");
						for (TestCase testCase : txrTestCase.getInputDataTestCases()) {
							String[] testData = testCase.getInputData();

							TxrState state = null;
							TxrAction command = null;
							MatchPair results = matcher.process2(testData, state, command);

							testCase.setStatus(results.matcherResults.isSuccess() ? TestCase.Status.PASSED : TestCase.Status.FAILED);

							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									if (!tableViewer.getTree().isDisposed()) {
										tableViewer.refresh(testCase);
									}
								}
							});
						}
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					} catch (TxrErrorInDocumentException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}.start();
		}

		tableViewer.setInput(allTestCases);

		return tableViewer;
	}

	private void processYaml(Yaml yaml, File yamlFile, List<TxrTestCase> allTestCases) {
		try (InputStream inputStream = yamlFile.toURI().toURL().openStream()) {
			TestConfiguration configuration = yaml.load(inputStream);
			
			File rootDirectory = yamlFile.getParentFile();
			configuration.setRoot(rootDirectory);
			
			for (TxrTestCase txrTestCase : configuration.txrFiles) {
				txrTestCase.setParents(configuration);
			}

			for (String redirect : configuration.redirects) {
				File secondaryYamlFile = new File(redirect);
				if (!secondaryYamlFile.exists()) {
					throw new RuntimeException("Cannot find file: " + secondaryYamlFile);
				}

				processYaml(yaml, secondaryYamlFile, allTestCases);
			}

			// Having processed the redirects, it may be that we are adding tests for TXR files
			// that have already been configured. Search for this based on the absolute path to the TXR file.
			for (TxrTestCase txrTestCase1 : configuration.txrFiles) {
				Optional<TxrTestCase> match = allTestCases.stream().filter(txrTestCase -> {
					try {
						return txrTestCase.getTxrResource().toURI().toURL().sameFile(txrTestCase1.getTxrResource().toURI().toURL());
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
				}).findFirst();
				if (match.isPresent()) {
					match.get().appendFrom(txrTestCase1);
				} else {
					allTestCases.add(txrTestCase1);
				}
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		treeViewer.getTree().setFocus();
	}

	@Persist
	public void save() {
		dirty.setDirty(false);
	}

}
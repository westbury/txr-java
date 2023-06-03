package txr_java_testframework;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.swt.graphics.Image;

public class TestCase {

	private String dataFile;
	
	public String description;
	
	public enum Status { PASSED, FAILED, NOT_RUN };
	
	private Status status;

	private TxrTestCase txrTestCase;

	private File resolvedDataFile;
	
	/** constructor for Snake */
	public TestCase() {
	}
	
	/** constructor for discovered tests */
	public TestCase(TxrTestCase txrTestCase, File dataFile) {
		this.dataFile = null;
		this.resolvedDataFile = dataFile;
		this.description = dataFile.getPath();
		this.status = Status.NOT_RUN;
		this.txrTestCase = txrTestCase;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
		this.status = Status.NOT_RUN;
	}
	
	public String getLabel() {
		return description;
	}

	public String[] getInputData() throws IOException {
		List<String> result = new ArrayList<>();
		try (InputStream inputStream = new FileInputStream(resolvedDataFile);
				Scanner scanner = new Scanner(inputStream, "UTF-8")) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.add(line);
			}
		}
		return result.toArray(new String[0]);
	}

	public Image getImage() {
		switch (status) {
		case PASSED:
			return Activator.createImage("icons/obj16/complete_status.png"); //$NON-NLS-1$
		case FAILED:
			return Activator.createImage("icons/obj16/error.png"); //$NON-NLS-1$
		default:
			return null;
		}
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	/** This must be called immediately after SnakeYaml has loaded the yaml contents, before anything else is called
	 * 
	 *  This property is required so that a test can be run against a selected TestCase outside the context of a TxrTestCase object.
	 *  This is the TxrTestCase against which tests are to be run. Do not use this TxrTestCase object when resolving relative paths because
	 *  it may be the wrong one.
	 */
	public void setTxrTestCase(TxrTestCase txrTestCase) {
		this.txrTestCase = txrTestCase;
	}
	
	public TxrTestCase getTxrTestCase() {
		return txrTestCase;
	}

	/** This must be called immediately after SnakeYaml has loaded the yaml contents, before anything else is called */
	public void resolve(TestConfiguration testConfiguration) {
		resolvedDataFile = testConfiguration.resolve(dataFile);
	}
}

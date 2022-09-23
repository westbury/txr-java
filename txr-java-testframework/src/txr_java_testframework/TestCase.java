package txr_java_testframework;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.swt.graphics.Image;

public class TestCase {

	private String label;
	
	private String txrFileName;
	
	private String inputDataFileName;
	
	public enum Status { PASSED, FAILED, NOT_RUN };
	
	TestCase(String label, String txrFileName, String inputDataFileName) {
		this.label = label;
		this.txrFileName = txrFileName;
		this.inputDataFileName = inputDataFileName;
		this.status = Status.NOT_RUN;
	};
	private Status status;
	
	public String getLabel() {
		return label;
	}

	public URL getTxrResource() {
		ClassLoader classLoader = getClass().getClassLoader();
		return classLoader.getResource(txrFileName);
	}

	public String[] getInputData() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		URL url = classLoader.getResource(inputDataFileName);
		List<String> result = new ArrayList<>();
		try (InputStream inputStream = url.openStream();
				Scanner scanner = new Scanner(inputStream)) {
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
}

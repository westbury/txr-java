package txr_java_testframework;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Image;

/**
 * This class represents a single TXR file together with all it's input data files.
 * 
 * @author nigel
 *
 */
public class TxrTestCase {

	
	String txrFile;
	public String description;
	private List<TestCase> dataFiles = Collections.emptyList();
	private List<String> dataDirectories = Collections.emptyList();
		
	private TestConfiguration testConfiguration;
	private List<File> resolvedDataDirectories;
	
	public void setTxrFile(String txrFile) {
		this.txrFile = txrFile;
	}
	
	public void setDataFiles(List<TestCase> dataFiles) {
		this.dataFiles = dataFiles;
	}

	public void setDataDirectories(List<String> dataDirectories) {
		this.dataDirectories = dataDirectories;
	}

	public void setParents(TestConfiguration testConfiguration) {
		this.testConfiguration = testConfiguration;
		
		for (TestCase dataFile : dataFiles) {
			dataFile.resolve(testConfiguration);
		}

		resolvedDataDirectories = dataDirectories.stream()
				.map(dataDirectory -> testConfiguration.resolve(dataDirectory))
				.filter(dataDirFile -> dataDirFile.exists()).collect(Collectors.toList());
	}
	
	public void appendFrom(TxrTestCase another) {
		// Snake's lists are not modifiable, so we copy here. Could be improved?
		this.dataFiles = new ArrayList<TestCase>(this.dataFiles);
		this.dataFiles.addAll(another.dataFiles);
		
		this.resolvedDataDirectories.addAll(another.resolvedDataDirectories);
	}

	List<TestCase> getInputDataTestCases() {
		List<TestCase> inputDataFiles = new ArrayList<>(dataFiles);

		for (File dataDirectory : resolvedDataDirectories) {
			if (dataDirectory.exists()) {
				for (File dataFile : dataDirectory.listFiles((dir, name) -> !name.equals(".DS_Store"))) {
					inputDataFiles.add(new TestCase(this, dataFile));
				}
			}
		}

		for (TestCase dataFile : dataFiles) {
			dataFile.setTxrTestCase(this);
		}
		
		return inputDataFiles;
	}
	
	public File getTxrResource() {
		return testConfiguration.resolve(txrFile);
	}

	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public TestConfiguration getConfiguration() {
		return testConfiguration;
	}

}

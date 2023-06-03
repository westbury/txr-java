package txr_java_testframework;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * The root yaml file object
 * @author nigel
 *
 */
public class TestConfiguration {

	public List<TxrTestCase> txrFiles = Collections.emptyList();
	
	/**
	 * A list of absolute paths to other yaml files, also with this schema. These files are read and
	 * all test cases and redirects in them are merged in. This is useful because the yaml configuration
	 * file in your home directory can redirect to yaml files from other repositories.
	 * 
	 */
	public List<String> redirects = Collections.emptyList();

	private File rootDirectory;

	public File resolve(String dataFileAsString) {
		File dataFile = new File(dataFileAsString);
		if (dataFile.isAbsolute()) {
			return dataFile;
		} else {
			return new File(rootDirectory, dataFileAsString);
		}
	}

	public void setRoot(File rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
}

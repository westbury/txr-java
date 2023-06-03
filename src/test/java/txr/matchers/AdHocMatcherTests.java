/**
 * 
 */
package txr.matchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import txr.matchers.DocumentMatcher.MatchPair;
import txr.parser.AST;
import txr.parser.Parser;
import txr.parser.TxrErrorInDocumentException;

/**
 * @author Nigel
 *
 */
public class AdHocMatcherTests {

	@Test
	public void JohnLewisTest() throws IOException, URISyntaxException, TxrErrorInDocumentException {
		Parser p = new Parser();

		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("john-lewis.txr").getFile());

		//		String content = new Scanner(url.openStream()).useDelimiter("\\Z").next();


		StringBuilder result = new StringBuilder("");


		try (Scanner scanner = new Scanner(file)) {

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append("\n");
			}

			scanner.close();

		} catch (IOException e) {
			e.printStackTrace();
		}





		//		List<String> lines = Files.readAllLines(Paths.get(url.toURI()), StandardCharsets.UTF_8);	}
		//		StringBuffer sb = new StringBuffer();
		//		for ()
		AST ast = p.parse(result.toString());
		assertEquals("[[[Symbol: foo, Symbol: bar]]]", ast.toString());
	}

	@Test
	public void AquacardTest() throws IOException, URISyntaxException, TxrErrorInDocumentException {
		DocumentMatcher matcher = createMatcherFromResource("aqua.txr");

		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource("aqua.txt");
		List<String> lines = Files.readAllLines(Paths.get(resource.toURI()), StandardCharsets.UTF_8);
		MatchPair results = matcher.process2(lines.toArray(new String[0]));
		
		assertEquals(true, results.matcherResults.isSuccess());
	}
	
	@Test
	public void JMFinnCashTest() throws IOException, URISyntaxException, TxrErrorInDocumentException {
		DocumentMatcher matcher = createMatcherFromResource("jmfinn-cash-statement.txr");

		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource("jmfinn-cash-statement.txt");
		List<String> lines = Files.readAllLines(Paths.get(resource.toURI()), StandardCharsets.UTF_8);
		MatchResults results = matcher.process(lines.toArray(new String[0]));
		
		assertEquals(9, results.getCollections(0).size());
	}
	
	private DocumentMatcher createMatcherFromResource(String resourceName) throws TxrErrorInDocumentException {
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource(resourceName);
		try (InputStream txrInputStream = resource.openStream()) {
			return new DocumentMatcher(txrInputStream, "UTF-8", null);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


}

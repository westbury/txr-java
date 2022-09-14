/**
 * 
 */
package txr.matchers;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
public class DebuggingTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	public void tearDown() throws Exception {
	}

	@Test
	public void CollectTest() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("Introduction\n"
				+ "\n"
				+ "@(collect)\n"
				+ "Title: @description\n"
				+ "Amount: £@amount\n"
				+ "\n"
				+ "@(until)\n"
				+ "Conclusion\n"
				+ "@(end)\n"
				+ "Conclusion\n"
				+ "Total: £@delivery\n");
//		assertEquals("[[Text: *Introduction*], [], [[Symbol: collect]], [Text: *Title: *, Ident: description], [Text: *Amount: £*, Ident: amount], [], [[Symbol: until]], [Text: *Conclusion*], [[Symbol: end]], [Text: *Conclusion*], [Text: *Total: £*, Ident: delivery]]", ast.toString());

		DocumentMatcher m = new DocumentMatcher(ast);
//		assertEquals("[Match on line: [Text: [*Introduction*]], Match on line: [], Collect body=[Match on line: [Text: [*Title: *], {Variable: Ident: description, Following: [EOL]}], Match on line: [Text: [*Amount: £*], {Variable: Ident: amount, Following: [EOL]}], Match on line: []] until=[Match on line: [Text: [*Conclusion*]]], Match on line: [Text: [*Conclusion*]], Match on line: [Text: [*Total: £*], {Variable: Ident: delivery, Following: [EOL]}]]", m.toString());

		String [] inputText = new String [] {
				"Introduction",
				"",
				"Title: Bananas",
				"Amount: £36",
				"",
				"Title: Oranges",
				"Amount: £42",
				"",
				"Conclusion",
				"Total: £78.00"
		};
		MatchPair matchedPair = m.process2(inputText);
		MatchResults matched = matchedPair.results;
		
		assertNotNull(matched);
		assertEquals(2, matched.getCollections(0).size());
		MatchResults bananaMatch = matched.getCollections(0).get(0);
		assertEquals("Bananas", bananaMatch.getVariable("description").text);
		assertEquals("36", bananaMatch.getVariable("amount").text);
		MatchResults orangeMatch = matched.getCollections(0).get(1);
		assertEquals("Oranges", orangeMatch.getVariable("description").text);
		assertEquals("42", orangeMatch.getVariable("amount").text);
		assertEquals("78.00", matched.getVariable("delivery").text);
		
//		0, 0
//		1, 1
//		2, collect(2, 5), until(8)
//		9, 8
//		10, 9 (total = 78.00)
	}

	@Test
	public void CollectTest_NoMatch() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("Introduction\n"
				+ "\n"
				+ "@(collect)\n"
				+ "Title: @description\n"
				+ "Amount: £@amount\n"
				+ "\n"
				+ "@(until)\n"
				+ "Conclusion\n"
				+ "@(end)\n"
				+ "Conclusion\n"
				+ "Total: £@delivery\n");

		DocumentMatcher m = new DocumentMatcher(ast);

		String [] inputText = new String [] {
				"Introduction",
				"",
				"Title: Bananas",
				"Amount: £36",
				"",
				"Title: Oranges",
				"Amount: £42",
				"",
				"Conclusionxxx",
				"Total: £78.00"
		};
		MatchPair matchedPair = m.process2(inputText);
		MatchResults matched = matchedPair.results;
		
		assertNull(matched);
		
//		0, 0
//		1, 1
//		2, collect(2, 5), until(8)
//		9, 8
//		10, 9 (total = 78.00)
	}

	/**
	 * Tests a failed match with a @(skip). The 'best' match should be returned.
	 * @throws TxrErrorInDocumentException 
	 */
	@Test
	public void SkipTest() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse(
			"@(skip)\n" +
			"Match A\n" +
			"Match B\n" +
			"Match C\n"
		);

		DocumentMatcher m = new DocumentMatcher(ast);

		String [] inputText = new String [] {
				"foo",
				"Match A",
				"bar",
				"Match A",
				"Match B",
				"baz",
				"Match A"
		};
		
		MatchPair matchedPair = m.process2(inputText);

		MatcherResultFailed failed1 = matchedPair.matcherResults.getFailedResult();
		assertTrue(failed1 instanceof MatcherResultSequenceFailed);
		MatcherResultFailed failed2 = ((MatcherResultSequenceFailed)failed1).failedMatch;
		assertTrue(failed2 instanceof MatcherResultSkipFailure);
		assertTrue(((MatcherResultSkipFailure)failed2).bestSkippedToLine == 3);
	}

}

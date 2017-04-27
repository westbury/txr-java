/**
 * 
 */
package txr.matchers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import txr.parser.AST;
import txr.parser.Parser;

/**
 * @author Nigel
 *
 */
public class DocumentMatcherTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void SimpleTest() {
		Parser p = new Parser();
		AST ast = p.parse("Match this\nvalue = @x\nMatch this @@ nine");
		assertEquals("[[Text: *Match this*], [Text: *value = *, Ident: x], [Text: *Match this @ nine*]]", ast.toString());

		DocumentMatcher m = new DocumentMatcher(ast);
		assertEquals("[Match on line: [Text: [*Match this*]], Match on line: [Text: [*value = *], {Variable: Ident: x, Following: [EOL]}], Match on line: [Text: [*Match this @ nine*]]]", m.toString());

		String [] inputText = new String [] {
				"Match this",
				"value = 27",
				"Match this @ nine"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
		assertEquals("27", matched.getVariable("x").text);
	}

	@Test
	public void CollectTest() {
		Parser p = new Parser();
		AST ast = p.parse("Introduction\n"
				+ "\n"
				+ "@(collect)\n"
				+ "Title: @description\n"
				+ "Amount: �@amount\n"
				+ "\n"
				+ "@(until)\n"
				+ "Conclusion\n"
				+ "@(end)\n"
				+ "Conclusion\n"
				+ "Total: �@delivery\n");
		assertEquals("[[Text: *Introduction*], [], [[Symbol: collect]], [Text: *Title: *, Ident: description], [Text: *Amount: �*, Ident: amount], [], [[Symbol: until]], [Text: *Conclusion*], [[Symbol: end]], [Text: *Conclusion*], [Text: *Total: �*, Ident: delivery]]", ast.toString());

		DocumentMatcher m = new DocumentMatcher(ast);
		assertEquals("[Match on line: [Text: [*Introduction*]], Match on line: [], Collect body=[Match on line: [Text: [*Title: *], {Variable: Ident: description, Following: [EOL]}], Match on line: [Text: [*Amount: �*], {Variable: Ident: amount, Following: [EOL]}], Match on line: []] until=[Match on line: [Text: [*Conclusion*]]], Match on line: [Text: [*Conclusion*]], Match on line: [Text: [*Total: �*], {Variable: Ident: delivery, Following: [EOL]}]]", m.toString());

		String [] inputText = new String [] {
				"Introduction",
				"",
				"Title: Bananas",
				"Amount: �36",
				"",
				"Title: Oranges",
				"Amount: �42",
				"",
				"Conclusion",
				"Total: �78.00"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
		assertEquals(2, matched.getCollections(0).size());
		MatchResults bananaMatch = matched.getCollections(0).get(0);
		assertEquals("Bananas", bananaMatch.getVariable("description").text);
		assertEquals("36", bananaMatch.getVariable("amount").text);
		MatchResults orangeMatch = matched.getCollections(0).get(1);
		assertEquals("Oranges", orangeMatch.getVariable("description").text);
		assertEquals("42", orangeMatch.getVariable("amount").text);
		assertEquals("78.00", matched.getVariable("delivery").text);
	}

	@Test
	public void CasesTest() {
		Parser p = new Parser();
		AST ast = p.parse("Introduction\n"
				+ "\n"
				+ "@(cases)\n"
				+ "Case1: @description\n"
				+ "@(or)\n"
				+ "Case2: @description\n"
				+ "@(and)\n"
				+ "Case3: @description\n"
				+ "@(end)\n"
				+ "Conclusion\n");
		assertEquals("[[Text: *Introduction*], [], [[Symbol: cases]], [Text: *Case1: *, Ident: description], [[Symbol: or]], [Text: *Case2: *, Ident: description], [[Symbol: and]], [Text: *Case3: *, Ident: description], [[Symbol: end]], [Text: *Conclusion*]]", ast.toString());

		DocumentMatcher m = new DocumentMatcher(ast);
		assertEquals("[Match on line: [Text: [*Introduction*]], Match on line: [], Cases[[Match on line: [Text: [*Case1: *], {Variable: Ident: description, Following: [EOL]}]][Match on line: [Text: [*Case2: *], {Variable: Ident: description, Following: [EOL]}]], [Match on line: [Text: [*Case3: *], {Variable: Ident: description, Following: [EOL]}]], , Match on line: [Text: [*Conclusion*]]]", m.toString());

		String [] inputText = new String [] {
				"Introduction",
				"",
				"Case2: Bananas",
				"Conclusion"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
		assertEquals(1, matched.getCollections(0).size());
		MatchResults bananaMatch = matched.getCollections(0).get(0);
		assertEquals("Bananas", bananaMatch.getVariable("description").text);
	}

	@Test
	public void MaybeWithMultipleMatchesTest() {
		Parser p = new Parser();
		AST ast = p.parse("Introduction\n"
				+ "@(maybe)\n"
				+ "Case1: @description\n"
				+ "@(or)\n"
				+ "@general\n"
				+ "Case1: @description\n"
				+ "@(end)\n"
				+ "Conclusion\n");

		DocumentMatcher m = new DocumentMatcher(ast);

		String [] inputText = new String [] {
				"Introduction",
				"Case1: Bananas",
				"Case1: Oranges",
				"Conclusion"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
		assertEquals(2, matched.getCollections(0).size());
		MatchResults bananaMatch = matched.getCollections(0).get(0);
		assertEquals("Bananas", bananaMatch.getVariable("description").text);
		MatchResults orangeMatch = matched.getCollections(0).get(1);
		assertEquals("Oranges", orangeMatch.getVariable("description").text);
	}

	@Test
	public void WhitespaceTest() {
		Parser p = new Parser();
		AST ast = p.parse("Match this\nvalue = @x\nMatch this @@ nine");
		assertEquals("[[Text: *Match this*], [Text: *value = *, Ident: x], [Text: *Match this @ nine*]]", ast.toString());

		DocumentMatcher m = new DocumentMatcher(ast);
		assertEquals("[Match on line: [Text: [*Match this*]], Match on line: [Text: [*value = *], {Variable: Ident: x, Following: [EOL]}], Match on line: [Text: [*Match this @ nine*]]]", m.toString());

		String [] inputText = new String [] {
				"Match this",
				"value = 27",
				"Match this @ nine"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
		assertEquals("27", matched.getVariable("x").text);
	}

}
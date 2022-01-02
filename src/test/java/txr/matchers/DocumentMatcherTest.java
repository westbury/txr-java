/**
 * 
 */
package txr.matchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import txr.parser.AST;
import txr.parser.Parser;
import txr.parser.TxrErrorInDocumentException;

/**
 * @author Nigel
 *
 */
public class DocumentMatcherTest {

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
	public void SimpleTest() throws TxrErrorInDocumentException {
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
		assertEquals("[[Text: *Introduction*], [], [[Symbol: collect]], [Text: *Title: *, Ident: description], [Text: *Amount: £*, Ident: amount], [], [[Symbol: until]], [Text: *Conclusion*], [[Symbol: end]], [Text: *Conclusion*], [Text: *Total: £*, Ident: delivery]]", ast.toString());

		DocumentMatcher m = new DocumentMatcher(ast);
		assertEquals("[Match on line: [Text: [*Introduction*]], Match on line: [], Collect body=[Match on line: [Text: [*Title:*], {Variable: Ident: description, Following: [EOL]}], Match on line: [Text: [*Amount:*, *£*], {Variable: Ident: amount, Following: [EOL]}], Match on line: []] until=[Match on line: [Text: [*Conclusion*]]], Match on line: [Text: [*Conclusion*]], Match on line: [Text: [*Total:*, *£*], {Variable: Ident: delivery, Following: [EOL]}]]", m.toString());

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
	public void CasesTest() throws TxrErrorInDocumentException {
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
		assertEquals("[Match on line: [Text: [*Introduction*]], Match on line: [], Cases[[Match on line: [Text: [*Case1:*], {Variable: Ident: description, Following: [EOL]}]][Match on line: [Text: [*Case2:*], {Variable: Ident: description, Following: [EOL]}]], [Match on line: [Text: [*Case3:*], {Variable: Ident: description, Following: [EOL]}]], ], Match on line: [Text: [*Conclusion*]]]", m.toString());
		String [] inputText = new String [] {
				"Introduction",
				"",
				"Case2: Bananas",
				"Conclusion"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
		assertEquals("Bananas", matched.getVariable("description").text);
	}

	@Test
	public void MaybeWithMultipleMatchesTest() throws TxrErrorInDocumentException {
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
	public void WhitespaceTest() throws TxrErrorInDocumentException {
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

	/**
	 * Tests that the @(assert) context does not require each match
	 * within an inner @(skip) to match.
	 * @throws TxrErrorInDocumentException 
	 */
	@Test
	public void SkipTest() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse(
	"@(skip)\n" +
	"Match A\n" +
	"@(assert)\n" +
	"@(skip)\n" +
	"Match B\n" +
	""
	);
//		assertEquals("[[Text: *Match this*], [Text: *value = *, Ident: x], [Text: *Match this @ nine*]]", ast.toString());

		DocumentMatcher m = new DocumentMatcher(ast);
//		assertEquals("[Match on line: [Text: [*Match this*]], Match on line: [Text: [*value = *], {Variable: Ident: x, Following: [EOL]}], Match on line: [Text: [*Match this @ nine*]]]", m.toString());

		String [] inputText = new String [] {
				"foo",
				"Match A",
				"bar",
				"Match B"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
	}

	@Test
	public void RegularExpressionTest() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@{transactiondate /\\d\\d [A-Z]+/} @{entereddate /\\d\\d [A-Z]+/} @description @{amount /\\d+\\.\\d\\d( CR)?/}");

		DocumentMatcher m = new DocumentMatcher(ast);

		String [] inputText = new String [] {
				"30 AUGUST 31 AUGUST PAYMENT RECEIVED - THANK YOU 100.00 CR"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
		assertEquals("30 AUGUST", matched.getVariable("transactiondate").text);
		assertEquals("31 AUGUST", matched.getVariable("entereddate").text);
		assertEquals("PAYMENT RECEIVED - THANK YOU", matched.getVariable("description").text);
		assertEquals("100.00 CR", matched.getVariable("amount").text);
	}

	@Test
	public void Section_6_13_RegexTest1() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@{amount /(\\d+,)?\\d+\\.\\d\\d/}");

		DocumentMatcher m = new DocumentMatcher(ast);

		String [] inputText = new String [] {
				"23,232.32"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
	}

	@Test
	public void Section_6_13_RegexTest2() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@{amount /[\\dA-Z]+/}");

		DocumentMatcher m = new DocumentMatcher(ast);

		String [] inputText = new String [] {
				"23A23X32"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
	}

	@Test
	public void Section_6_13_WhitespaceTest1() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("foo bar  baz");

		DocumentMatcher m = new DocumentMatcher(ast);

		MatchResults matched1 = m.process("foo    bar  baz");
		assertNotNull(matched1);

		MatchResults matched2 = m.process("foo    bar  baz ");
		assertNull(matched2);

		MatchResults matched3 = m.process(" foo    bar  baz");
		assertNull(matched3);

		MatchResults matched4 = m.process("foobar  baz");
		assertNull(matched4);

		MatchResults matched5 = m.process("foo\tbar  baz");
		assertNull(matched5);

		MatchResults matched6 = m.process("foo  bar    baz");
		assertNull(matched6);
	}

	@Test
	public void Section_6_13_WhitespaceTest2() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse(" foo bar  baz ");

		DocumentMatcher m = new DocumentMatcher(ast);

		MatchResults matched1 = m.process(" foo    bar  baz");
		assertNull(matched1);

		MatchResults matched2 = m.process("foo    bar  baz");
		assertNull(matched2);

		MatchResults matched3 = m.process("   foo    bar  baz   ");
		assertNotNull(matched3);
	}

	@Test
	public void Section_6_13_RegexTest10() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@{currency /\\w\\w\\w/} @{amount /[\\d,.]+/} @{balance /[\\d,.]+/}");

		DocumentMatcher m = new DocumentMatcher(ast);

		String [] inputText = new String [] {
				"CAD  2,860.13  3,910.13"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
	}

	@Test
	public void regex_alternative_text() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@{cardtype /Visa|Master Card/}");

		DocumentMatcher m = new DocumentMatcher(ast);

		String [] inputText = new String [] {
				"Master Card"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
	}

	@Test
	public void Ebay_detail_problem() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("Ending in @{lastfourdigits}@{cardtype /Visa|Master Card/} credit card ending in @{lastfourdigits}");

		DocumentMatcher m = new DocumentMatcher(ast);

		String [] inputText = new String [] {
				"Ending in 1082Master Card credit card ending in 1082"
		};
		MatchResults matched = m.process(inputText);
		assertNotNull(matched);
	}

}

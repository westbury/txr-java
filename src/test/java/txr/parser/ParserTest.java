/**
 * 
 */
package txr.parser;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Nigel
 *
 */
public class ParserTest {

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
		AST ast = p.parse("Match this\nvalue = @(x)\nMatch this @@ nine");
		assertEquals("[[Text: *Match this*], [Text: *value = *, [Symbol: x]], [Text: *Match this @ nine*]]", ast.toString());
	}

	@Test
	public void Section_6_15_CharacterLiteralTest() {
		Parser p = new Parser();
		AST ast = p.parse("@(#\\nul #\\linefeed #\\pnul #\\x41 #\\o54 #\\))");
		assertEquals("[[[Char: 0, Char: 10, Char: 56320, Char: 65, Char: 44, Char: 41]]]", ast.toString());
	}

	@Test
	public void Section_6_16_BasicTest() { 
		Parser p = new Parser();
		AST ast = p.parse("@(\"foo \\x21;; \\51; \\n \\t  bar\")");
		assertEquals("[[[String: *foo !; ) \n \t  bar*]]]", ast.toString());
	}

	@Test
	public void Section_6_16_Continuation1Test() {
		Parser p = new Parser();
		AST ast = p.parse("@(\"foo   \\\n  bar\")");
		assertEquals("[[[String: *foobar*]]]", ast.toString());
	}

	@Test
	public void Section_6_16_Continuation2Test() {
		Parser p = new Parser();
		AST ast = p.parse("@(\"foo \\  \\\n  bar\")");
		assertEquals("[[[String: *foo  bar*]]]", ast.toString());
	}

	@Test
	public void Section_6_16_Continuation3Test() {
		Parser p = new Parser();
		AST ast = p.parse("@(\"foo   \\\n \\  bar\")");
		assertEquals("[[[String: *foo  bar*]]]", ast.toString());
	}

	@Test
	public void IntegerTest() {
		Parser p = new Parser();
		AST ast = p.parse("@(#o24 #x3f #b-1101)");
		assertEquals("[[[Integer: 20, Integer: 63, Integer: -13]]]", ast.toString());
	}

	@Test
	public void Section_6_20_IntegerTest() {
		Parser p = new Parser();
//		AST ast = p.parse("@(123 -34 +0 -0 +234483527304983792384729384723234)");
		AST ast = p.parse("@(123 -34 +0 -0 +2344835273049837)");
		assertEquals("[[[Integer: 123, Integer: -34, Integer: 0, Integer: 0, Integer: 2344835273049837]]]", ast.toString());
	}

	@Test
	public void Section_6_20_FloatingPointTest() {
		Parser p = new Parser();
		AST ast = p.parse("@(.123 123. 1E-3 20E40 .9E1 9.e19 -.5 +3E+3 1.E5)");
		assertEquals("[[[Float: 0.123, Float: 123.0, Float: 0.001, Float: 2.0E41, Float: 9.0, Float: 9.0E19, Float: -0.5, Float: 3000.0, Float: 100000.0]]]", ast.toString());
	}

	@Test
	public void Section_6_20_BadFloatingPoint1() {
		Parser p = new Parser();
		AST ast = p.parse("@(123E)");
		assertEquals("[[[Symbol: 123E]]]", ast.toString());
	}

	@Test (expected = RuntimeException.class)
	public void Section_6_20_BadFloatingPoint2() {
		Parser p = new Parser();
		p.parse("@(1.0E-)");
	}

	@Test (expected = RuntimeException.class)
	public void Section_6_20_BadFloatingPoint3() {
		Parser p = new Parser();
		p.parse("@(1.0E)");
	}

	@Test (expected = RuntimeException.class)
	public void Section_6_20_BadFloatingPoint4() {
		Parser p = new Parser();
		p.parse("@(1.E)");
	}

	@Test (expected = RuntimeException.class)
	public void Section_6_20_BadFloatingPoint5() {
		Parser p = new Parser();
		p.parse("@(.e)");
	}

	@Test
	public void Section_6_21_Test() {
		Parser p = new Parser();
		AST ast = p.parse("@(foo ; this is a comment\n  bar ; this is another comment\n  )");
		assertEquals("[[[Symbol: foo, Symbol: bar]]]", ast.toString());
	}

	@Test
	public void CollectTest() {
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
	}

	@Test
	public void JohnLewisTest() throws IOException, URISyntaxException {
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
}

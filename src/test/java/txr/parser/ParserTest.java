/**
 * 
 */
package txr.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import txr.matchers.DocumentMatcher;
import txr.matchers.MatchResults;
import txr.matchers.TxrException;

/**
 * @author Nigel
 *
 */
public class ParserTest {

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
		AST ast = p.parse("Match this\nvalue = @(x)\nMatch this @@ nine");
		assertEquals("[[Text: *Match this*], [Text: *value = *, [Symbol: x]], [Text: *Match this @ nine*]]", ast.toString());
	}

	@Test
	public void Section_6_12_KeywordTest() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@(collect :maxgap 12 :mingap 5)");
		assertEquals("[[[Symbol: collect, Symbol: :maxgap, Integer: 12, Symbol: :mingap, Integer: 5]]]", ast.toString());
	}

	@Test
	public void Section_6_12_KeywordWithInvalidFollowingWhitespaceTest() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@(collect :maxgap 12 :mingap 5)  \n@line\n@(end)");
		assertEquals("[[[Symbol: collect, Symbol: :maxgap, Integer: 12, Symbol: :mingap, Integer: 5], Text: *  *], [Ident: line], [[Symbol: end]]]", ast.toString());

		TxrException exception = assertThrows(TxrException.class, () -> {
			new DocumentMatcher(ast);
	    });
	    assertTrue(exception.getMessage().contains("expected something"));
	}

	@Test
	public void Section_6_15_CharacterLiteralTest() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@(#\\nul #\\linefeed #\\pnul #\\x41 #\\o54 #\\))");
		assertEquals("[[[Char: 0, Char: 10, Char: 56320, Char: 65, Char: 44, Char: 41]]]", ast.toString());
	}

	@Test
	public void Section_6_16_BasicTest() throws TxrErrorInDocumentException { 
		Parser p = new Parser();
		AST ast = p.parse("@(\"foo \\x21;; \\51; \\n \\t  bar\")");
		assertEquals("[[[String: *foo !; ) \n \t  bar*]]]", ast.toString());
	}

	@Test
	public void Section_6_16_Continuation1Test() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@(\"foo   \\\n  bar\")");
		assertEquals("[[[String: *foobar*]]]", ast.toString());
	}

	@Test
	public void Section_6_16_Continuation2Test() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@(\"foo \\  \\\n  bar\")");
		assertEquals("[[[String: *foo  bar*]]]", ast.toString());
	}

	@Test
	public void Section_6_16_Continuation3Test() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@(\"foo   \\\n \\  bar\")");
		assertEquals("[[[String: *foo  bar*]]]", ast.toString());
	}

	@Test
	public void IntegerTest() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@(#o24 #x3f #b-1101)");
		assertEquals("[[[Integer: 20, Integer: 63, Integer: -13]]]", ast.toString());
	}

	@Test
	public void Section_6_20_IntegerTest() throws TxrErrorInDocumentException {
		Parser p = new Parser();
//		AST ast = p.parse("@(123 -34 +0 -0 +234483527304983792384729384723234)");
		AST ast = p.parse("@(123 -34 +0 -0 +2344835273049837)");
		assertEquals("[[[Integer: 123, Integer: -34, Integer: 0, Integer: 0, Integer: 2344835273049837]]]", ast.toString());
	}

	@Test
	public void Section_6_20_FloatingPointTest() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@(.123 123. 1E-3 20E40 .9E1 9.e19 -.5 +3E+3 1.E5)");
		assertEquals("[[[Float: 0.123, Float: 123.0, Float: 0.001, Float: 2.0E41, Float: 9.0, Float: 9.0E19, Float: -0.5, Float: 3000.0, Float: 100000.0]]]", ast.toString());
	}

	@Test
	public void Section_6_20_BadFloatingPoint1() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@(123E)");
		assertEquals("[[[Symbol: 123E]]]", ast.toString());
	}

	@Test
	public void Section_6_20_BadFloatingPoint2() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			p.parse("@(1.0E-)");
	    });
	    assertTrue(exception.getMessage().contains("Invalid floating-point constant"));
	}

	@Test
	public void Section_6_20_BadFloatingPoint3() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			p.parse("@(1.0E)");
	    });
	    assertTrue(exception.getMessage().contains("Invalid floating-point constant"));
	}

	@Test
	public void Section_6_20_BadFloatingPoint4() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			p.parse("@(1.E)");
	    });
	    assertTrue(exception.getMessage().contains("Invalid floating-point constant"));
	}

	@Test
	public void Section_6_20_BadFloatingPoint5() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			p.parse("@(.e)");
	    });
	    assertTrue(exception.getMessage().contains("a '.' appears, but no digits before or after it so not a valid floating-point number"));
	}

	@Test
	public void Section_6_21_Test() throws TxrErrorInDocumentException {
		Parser p = new Parser();
		AST ast = p.parse("@(foo ; this is a comment\n  bar ; this is another comment\n  )");
		assertEquals("[[[Symbol: foo, Symbol: bar]]]", ast.toString());
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
	}

}

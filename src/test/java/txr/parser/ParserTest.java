/**
 * 
 */
package txr.parser;

import static org.junit.Assert.*;

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
	public void IntegerTest() {
		Parser p = new Parser();
		AST ast = p.parse("@(#o24 #x3f #b-1101)");
		assertEquals("[[[Integer: 20, Integer: 63, Integer: -13]]]", ast.toString());
	}
	
	@Test
	public void Section_6_21_Test() {
		Parser p = new Parser();
		AST ast = p.parse("@(foo ; this is a comment\n  bar ; this is another comment\n  )");
		assertEquals("[[[Symbol: foo, Symbol: bar]]]", ast.toString());
	}
	
}

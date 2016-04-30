package txr.parser;

import java.util.ArrayList;
import java.util.List;


/**
 * This class parses an input stream and produces an abstract
 * syntax tree.
 * <P>
 * The output is as follows:
 * 
 * - blocks of lines of text are returned as a line of match items, with one
 * match item per line.
 * - all comments are removed.  Only @; is supported, the obsolete @#
 * is not supported.
 * - @@ are replaced in the text by @
 * - All other @ occurences are replaced by the appropriate node object.
 * 
 * @author Nigel
 *
 */
public class Parser {

	String query;

	int i = 0;

	Line currentLine;

	private AST ast = new AST();

	public AST parse(String query) {
		this.query = query;

		currentLine = new Line();

		int textStart = 0;
		StringBuffer currentText = new StringBuffer();

		int queryLength = query.length();
		do {
			char c = query.charAt(i);

			switch (c) {
			case '\r':
			case '\n':
				currentText.append(query.substring(textStart, i));
				if (currentText.length() != 0) {
					currentLine.append(new TextNode(currentText.toString()));
					currentText = new StringBuffer();
				}
				appendThisLine();
				i += 1;
				textStart = i;
				break;

			case '@':
				currentText.append(query.substring(textStart, i));

				if (query.charAt(i+1) == '@') {
					currentText.append('@');
					i += 2;
				} else {
					if (currentText.length() != 0) {
						currentLine.append(new TextNode(currentText.toString()));
						currentText = new StringBuffer();
					}

					if (query.charAt(i+1) == ';') {
						i += 2;

						/* If a comment is at the start of a line then the
					 entire line is a removed (the matcher does not require a blank
					line).
						 */
						if (!currentLine.isEmpty()) {
							appendThisLine();
						}

						eatRestOfLine();
					} else if (query.charAt(i+1) == '(') {
						i += 2;
						List<SubExpression> subExpressions = parseExpression();
						currentLine.append(new Expr(subExpressions));
					} else if (query.charAt(i+1) == '*') {
						i += 2;
						Ident ident = parseSidentOrBident();
						ident.setLongMatch(true);
						currentLine.append(ident);
					} else {
						i += 1;
						Ident ident = parseSidentOrBident();
						currentLine.append(ident);
					}
				}

				textStart = i;
				break;

			default:
				i++;
			}
		} while (i < queryLength);

		currentText.append(query.substring(textStart, i));
		if (currentText.length() != 0) {
			currentLine.append(new TextNode(currentText.toString()));
			currentText = new StringBuffer();
		}

		if (!currentLine.isEmpty()) {
			appendThisLine();
		}

		return ast;
	}

	/**
	 * Eats the rest of the line, leaving the position at the
	 * start of the following line.
	 */
	private void eatRestOfLine() {
		while (query.charAt(i) != '\n') {
			i++;
		}
		i++;
	}

	/**
	 * Parses an expression list, being expressions inside
	 * brackets.
	 * 
	 * @return
	 */
	private List<SubExpression> parseExpression() {
		List<SubExpression> result = new ArrayList<>();

		while (query.charAt(i) == ' ' || query.charAt(i) == '\n') i++;

		while (query.charAt(i) != ')') {
			char c = query.charAt(i);

			SubExpression subExpression;

			switch (c) {
			case '#':
				char c2 = query.charAt(i+1);
				switch (c2) {
				case '/':
					// A regular expression
					i += 2;
					subExpression = parseRegularExpression();
					break;
				case '\\':
					//http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=194 A character literal
					i += 2;
					subExpression = parseCharacterLiteral();
					break;
				case 'x':
					// A hexadecimal literal
					i += 2;
					subExpression = parseHexadecimalLiteral();
					break;
				case 'o':
					// An octal literal
					i += 2;
					subExpression = parseOctalLiteral();
					break;
				case 'b':
					// A binary literal
					i += 2;
					subExpression = parseBinaryLiteral();
					break;
				default:
					throw new RuntimeException("invalid subexpression");
				}
				break;

			case '"':
				// A string literal
				i += 1;
				subExpression = parseStringLiteral();
				break;

			case '`':
				// A quasi-literal
				i += 1;
				subExpression = parseQuasiLiteral();
				break;

			case '(':
				// A nested expr
				i += 1;
				List<SubExpression> subExpressions = parseExpression();
				subExpression = new NestedExpr(subExpressions);
				break;

			case ';':
				// A comment.  Ignore the rest of the line
				eatRestOfLine();
				subExpression = null;
				break;
				
			default:
				subExpression = parseLidentOrNumber();
			}

			if (subExpression != null) {
				result.add(subExpression);
			}

			while (query.charAt(i) == ' ' || query.charAt(i) == '\n') i++;
		}

		i++; // pass the ')'

		return result;
	}

	private SubExpression parseQuasiLiteral() {
		throw new UnsupportedOperationException();
	}

	private SubExpression parseStringLiteral() {
		throw new UnsupportedOperationException();
	}

	private SubExpression parseBinaryLiteral() {
		int start = i;

		int	c = query.charAt(i);
		if (c == '+' || c == '-') {
			i++;
			c = query.charAt(i);
		}

		if (!isBinaryDigit(c)) {
			throw new RuntimeException("Binary digit of 0 or 1 expected.");
		}

		do {
			i++;
			c = query.charAt(i);
		} while (isBinaryDigit(c));

		String binaryAsText = query.substring(start, i);
		int value = Integer.parseInt(binaryAsText, 2);
		return new IntegerLiteral(value);
	}

	private boolean isBinaryDigit(int c) {
		return c == '0' || c == '1';
	}

	private SubExpression parseOctalLiteral() {
		int start = i;

		int	c = query.charAt(i);
		if (c == '+' || c == '-') {
			i++;
			c = query.charAt(i);
		}

		if (!isOctalDigit(c)) {
			throw new RuntimeException("Octal digit of 0 to 7 expected.");
		}

		do {
			i++;
			c = query.charAt(i);
		} while (isOctalDigit(c));

		String octalAsText = query.substring(start, i);
		int value = Integer.parseInt(octalAsText, 8);
		return new IntegerLiteral(value);
	}

	private boolean isOctalDigit(int c) {
		return c >= '0' && c <= '7';
	}

	private SubExpression parseHexadecimalLiteral() {
		int start = i;

		int	c = query.charAt(i);
		if (c == '+' || c == '-') {
			i++;
			c = query.charAt(i);
		}

		if (!isHexadecimalDigit(c)) {
			throw new RuntimeException("Hexadecimal digit of 0 to 9, A to F expected.");
		}

		do {
			i++;
			c = query.charAt(i);
		} while (isHexadecimalDigit(c));

		String hexadecimalAsText = query.substring(start, i);
		int value = Integer.parseInt(hexadecimalAsText, 16);
		return new IntegerLiteral(value);
	}

	private boolean isHexadecimalDigit(int c) {
		return (c >= '0' && c <= '9')
				|| (c >= 'A' && c <= 'F')
				|| (c >= 'a' && c <= 'f');
	}

	private SubExpression parseCharacterLiteral() {
		throw new UnsupportedOperationException();
	}

	private SubExpression parseRegularExpression() {
		throw new UnsupportedOperationException();
	}

	/**
	 * This may be a number or a lident.  A lident may
	 * consist of any combination of letters, digits,
	 * and ! $ % & * + - < = > ? \ _ ˜ /
	 * 
	 * A number
	 * @return
	 */
	private SubExpression parseLident() {
		int start = i;

		char c = query.charAt(i);
		do {
			i++;
			c = query.charAt(i);
		} while (isValidLidentChar(c));

		if (c != ' ' && c != ')') {
			throw new RuntimeException("Invalid character in lident: " + c);
		}
		return new Symbol(query.substring(start, i));
	}

	/**
	 * This may be a number or a lident.  A lident may
	 * consist of any combination of letters, digits,
	 * and ! $ % & * + - < = > ? \ _ ˜ /
	 * 
	 * A number
	 * @return
	 */
	private SubExpression parseLidentOrNumber() {
		//		// First attempt to parse as a number
		//		// If that fails, see if it is a valid lident
		//	
		//		boolean isNegated = false;
		//		if (query.charAt(i) == '+') {
		//			i++;
		//		} else if (query.charAt(i) == '-') {
		//			negate = true;
		//			i++;
		//		}
		//		
		//		int 
		//		c = query.charAt(i);
		//		while ()
		//	{
		//		i += 1;
		//		int value = parseInteger();
		//		expr = new IntegerLiteral(value);
		//	}
		//		break;
		//		
		//	case '-':
		//	{
		//		i += 1;
		//		int value = parseInteger();
		//		expr = new IntegerLiteral(-value);
		//	}
		//		break;
		//		expr = new IntegerLiteral(-value);
		//		if (Character.isDigit(c)) {
		//			int value = parseInteger();
		//			expr = new IntegerLiteral(value);
		//		}

		// If not an integer or floating point number, it's a symbol
		return parseLident();
	}

	private void appendThisLine() {
		ast.append(currentLine);
		currentLine = new Line();

	}

	private Ident parseSidentOrBident() {
		if (query.charAt(i) == '{') {
			int start = i+1;

			char c;
			do {
				i++;
				c = query.charAt(i);
			} while (isValidBidentChar(c));

			if (c != '}') {
				throw new RuntimeException("Invalid character in bident: " + c);
			}
			String id = query.substring(start, i);
			return new Ident(id);
		} else {
			return parseSident();
		}
	}

	private Ident parseSident() {
		int start = i;

		char c = query.charAt(i);
		do {
			i++;
			c = query.charAt(i);
		} while (isValidBidentChar(c));

		if (c != '}') {
			throw new RuntimeException("Invalid character in bident: " + c);
		}
		return new Ident(query.substring(start,  i));
	}

	private boolean isValidBidentChar(char c) {
		switch (c) {
		case '!':
		case '$':
		case '%':
		case '&':
		case '*':
		case '+':
		case '-':
		case '<':
		case '=':
		case '>':
		case '?':
		case '\\':
		case '_':
			return true;
		default:
			return Character.isAlphabetic(c)
					|| Character.isDigit(c);
		}
	}

	private boolean isValidLidentChar(char c) {
		return isValidBidentChar(c) || c == '/';
	}
}

package txr.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import txr.matchers.CharsFromInputLineReader;


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

	public AST parse(String query) throws TxrErrorInDocumentException {
		this.query = query;

		currentLine = new Line();

		int textStart = 0;
		StringBuffer currentText = new StringBuffer();

		/** used for error reporting only */
		int currentLineNumber = 1;
		
		TxrErrorInDocumentException documentErrors = new TxrErrorInDocumentException();
		
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
				currentLineNumber++;
				
				i += 1;
				textStart = i;
				break;

			case '@':
				currentText.append(query.substring(textStart, i));
				i++;
				
				/*
				 * White space is allowed after the '@'. This can be used for
				 * indentation on lines where the '@' must be the first
				 * character.
				 */
				while (query.charAt(i) == ' ' || query.charAt(i) == '\t') {
					i++;
				}
				
				if (query.charAt(i) == '@') {
					currentText.append('@');
					i++;
				} else {
					if (currentText.length() != 0) {
						currentLine.append(new TextNode(currentText.toString()));
						currentText = new StringBuffer();
					}

					if (query.charAt(i) == ';') {
						i++;

						/*
						 * If a comment is at the start of a line then the
						 * entire line is a removed (the matcher does not
						 * require a blank line).
						 */
						if (!currentLine.isEmpty()) {
							appendThisLine();
						}

						eatRestOfLine();
					} else if (query.charAt(i) == '(') {
						i++;
						try {
							List<SubExpression> subExpressions = parseExpression();
							currentLine.append(new Expr(subExpressions));
						} catch (TxrErrorOnLineException e) {
							documentErrors.add(currentLineNumber, e);
						}
					} else if (query.charAt(i) == '*') {
						i++;
						try {
							Ident ident = parseSidentOrBident();
							ident.setLongMatch(true);
							currentLine.append(ident);
						} catch (TxrErrorOnLineException e) {
							// TODO how do we recover?
							documentErrors.add(currentLineNumber, e);
						}
					} else {
						try {
							Ident ident = parseSidentOrBident();
							currentLine.append(ident);
						} catch (TxrErrorOnLineException e) {
							// TODO how do we recover?
							documentErrors.add(currentLineNumber, e);
						}
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

		if (documentErrors.hasErrors()) {
			throw documentErrors;
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
	 * Eats characters up to a closing brace or the end of the line.  This method
	 * is used only as an attempt to recover the parser.
	 * <P>
	 * The position is left on the closing brace or the newline character.
	 */
	private void eatToCloseBracesOrEndOfLine() {
		while (query.charAt(i) != ')' && query.charAt(i) != '\n') {
			i++;
		}
	}

	/**
	 * Parses an expression list, being expressions inside
	 * brackets.
	 * 
	 * @return
	 */
	private List<SubExpression> parseExpression() throws TxrErrorOnLineException {
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
					subExpression = parseRegularExpression('/');
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
					// As a best attempt at recovering the parser, move on to the next ')'
					// or to the end of the line.
					eatToCloseBracesOrEndOfLine();
					
					throw new TxrErrorOnLineException(i, "Invalid subexpression. The character '" + c2 + " is not a valid character to follow #.");
				}
				break;

			case ':':
				// A keyword symbol
				// Is this correct?  Can a keyword really
				// contain all the lident characters???
				// Note that ':' is not a lident character but
				// the first character is not validated by the following method.
				subExpression = parseLident();
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

	/**
	 * We are initially positioned at the first character
	 * after the opening double quote.  On return we are
	 * positioned at the first character after the closing
	 * double quote.
	 * 
	 * @return
	 * @throws TxrErrorOnLineException 
	 */
	private SubExpression parseStringLiteral() throws TxrErrorOnLineException {
		StringBuffer result = new StringBuffer();

		int start = i;

		char c = query.charAt(i);
		do {
			if (c == '\\') {
				{
					int j = i;
					if (query.charAt(i+1) == '\n') {
						/*
						 * This is a little complicated because we want to remove
						 * trailing spaces, but only back to the last escaped space.
						 * As escaped spaces are appended separately, the start position
						 * would be following any escaped space.
						 */
						while (j > start && query.charAt(j-1) == ' ') {
							j--;
						}
					}
					if (j > start) {
						result.append(query.substring(start, j));
					}
				}

				i++;
				c = query.charAt(i);
				switch (c) {
				case '\\':
				case '"':
					start = i;
					break;
				case 'n':
					result.append('\n');
					start = ++i;
					break;
				case 't':
					result.append('\t');
					start = ++i;
					break;
				case ' ':
					result.append(' ');
					start = ++i;
					break;
				case 'x':
					i++;
					char hexChar = parseHexChar();
					result.append(hexChar);
					if (query.charAt(i) == ';') {
						i++;
					}
					start = i;
					break;
				case '\n':
					/*
					 * A backslash as the very last character of a line
					 * indicates a continuation.  Trailing spaces at the
					 * end of the current line have already been removed.
					 * We must eat all characters till the first non-space
					 * character in the following line.
					 */
					do {
						i++;
						c = query.charAt(i);
					} while (c == ' ');
					start = i;
					break;
				default:
					if (Character.isDigit(c)) {
						char octalChar = parseOctalChar();
						result.append(octalChar);
						if (query.charAt(i) == ';') {
							i++;
						}
						start = i;
					} else {
						throw new TxrErrorOnLineException(start, i, "Backslash followed by invalid character.");
					}
				}
			} else {
				i++;
			}

			c = query.charAt(i);
		} while (c != '"');

		if (i > start) {
			result.append(query.substring(start, i));
		}

		i++;  // Pass the closing double quote
		
		return new StringLiteral(result.toString());
	}

	/**
	 * On entry the input points to the first octal digit.  On exit
	 * the input points to the first following character that is
	 * not an octal digit.
	 * 
	 * @return
	 * @throws TxrErrorOnLineException 
	 */
	private char parseOctalChar() throws TxrErrorOnLineException {
		int start = i;
		char c = query.charAt(i);
		while (isOctalDigit(c)) {
			i++;
			c = query.charAt(i);
		}
		
		if (i - start == 0) {
			throw new TxrErrorOnLineException(start-2, start+1, "Octal digits expected.");
		}

		String octalAsString = query.substring(start, i);
		int value = Integer.parseInt(octalAsString, 8);
		
		return (char)value;
	}

	/**
	 * On entry the input points to the first hex digit.  On exit
	 * the input points to the first following character that is
	 * not a hex digit.
	 * 
	 * @return
	 */
	private char parseHexChar() {
		int start = i;
		char c = query.charAt(i);
		while (isHexadecimalDigit(c)) {
			i++;
			c = query.charAt(i);
		}
		
		if (i - start == 0) {
			throw new RuntimeException("Hexadecimal digits expected.");
		}

		String hexadecimalAsString = query.substring(start, i);
		int value = Integer.parseInt(hexadecimalAsString, 16);
		
		return (char)value;
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

	private SubExpression parseCharacterLiteral() throws TxrErrorOnLineException {
		int start = i;

		char c = query.charAt(i);
		switch (Character.toLowerCase(c)) {
		case 'x':
			if (isHexadecimalDigit(query.charAt(i+1))) {
				i++;
				char hexadecimalChar = parseHexChar();
				return new CharacterLiteral(hexadecimalChar);
			}
			// If no hexadecimal digit, fall through to get 'x' or 'X'
			break;
		case 'o':
			if (isOctalDigit(query.charAt(i+1))) {
				i++;
				char octalChar = parseOctalChar();
				return new CharacterLiteral(octalChar);
			}
			// If no octal digit, fall through to get 'o' or 'O'
			break;
		case 'a':
			if (query.substring(start, start+5).equalsIgnoreCase("alarm")) {
				i += 5;
				return new CharacterLiteral((char)7); 
			}
			break;
		case 'b':
			if (query.substring(start, start+9).equalsIgnoreCase("backspace")) {
				i += 9;
				return new CharacterLiteral((char)8); 
			}
			break;
		case 'e':
			if (query.substring(start, start+3).equalsIgnoreCase("esc")) {
				i += 3;
				return new CharacterLiteral((char)27); 
			}
			break;
		case 'l':
			if (query.substring(start, start+8).equalsIgnoreCase("linefeed")) {
				i += 8;
				return new CharacterLiteral((char)10); 
			}
			break;
		case 'n':
			if (query.substring(start, start+7).equalsIgnoreCase("newline")) {
				i += 7;
				return new CharacterLiteral((char)10); 
			} else if (query.substring(start, start+3).equalsIgnoreCase("nul")) {
				i += 3;
				return new CharacterLiteral((char)0); 
			}
			break;
		case 'p':
			if (query.substring(start, start+4).equalsIgnoreCase("pnul")) {
				i += 4;
				return new CharacterLiteral('\uDC00'); 
			} else if (query.substring(start, start+4).equalsIgnoreCase("page")) {
				i += 4;
				return new CharacterLiteral((char)12); 
			}
			break;
		case 'r':
			if (query.substring(start, start+6).equalsIgnoreCase("return")) {
				i += 6;
				return new CharacterLiteral((char)13); 
			}
			break;
		case 's':
			if (query.substring(start, start+5).equalsIgnoreCase("space")) {
				i += 5;
				return new CharacterLiteral((char)32); 
			}
			break;
		case 't':
			if (query.substring(start, start+3).equalsIgnoreCase("tab")) {
				i += 3;
				return new CharacterLiteral((char)9); 
			}
			break;
		case 'v':
			if (query.substring(start, start+4).equalsIgnoreCase("vtab")) {
				i += 4;
				return new CharacterLiteral((char)11); 
			}
			break;
		}
		
		i += 1;
		return new CharacterLiteral(c); 
	}

	/**
	 * See section 6.13.  On entry, i will be positioned
	 * at the first character after the '/', being the first
	 * character of the regular expression.  On exit, i will be
	 * positioned at the first character after the terminating '/'.
	 * <P>
	 * This method is also called recursively when stuff in in brackets ( and ).
	 * Hence why we have parameter terminatingChar.
	 * 
	 * @return an implementation of SubExpression that represents the
	 * 			regular expression
	 * @throws TxrErrorOnLineException 
	 */
	private RegularExpression parseRegularExpression(char terminatingChar) throws TxrErrorOnLineException {
		List<RegexMatcher> regexMatchers = new ArrayList<>();
		List<RegularExpression> alternatives = new ArrayList<>();
		
		int startForErrorPurposes = i - 1;
		
		char c = query.charAt(i);
		i++;
		
		do {
			final char c1 = c;
			
			switch (c1) {
			case '.':
				regexMatchers.add(new SingleCharMatcher(ch -> true));
				break;
				
			case '\\':
				final char c2 = query.charAt(i);
				i++;

				CharMatcher charMatcher;
				
				switch (c2) {
				case 's':
					charMatcher = ch -> isWhiteSpace(ch);
					break;
					
				case 'w':
					charMatcher = ch -> isWordCharacter(ch);
					break;
					
				case 'd':
					charMatcher = ch -> isDigit(ch);
					break;
					
				case 'S':
					charMatcher = ch -> !isWhiteSpace(ch);
					break;
					
				case 'W':
					charMatcher = ch -> !isWordCharacter(ch);
					break;
					
				case 'D':
					charMatcher = ch -> !isDigit(ch);
					break;
					
				case '\\':
					charMatcher = ch -> ch == '\\';
					break;
					
				case '/':
					charMatcher = ch -> ch == '/';
					break;
					
				case '.':
					charMatcher = ch -> ch == '.';
					break;
					
				case '[':
					charMatcher = ch -> ch == '[';
					break;
					
				case '-':
					charMatcher = ch -> ch == '-';
					break;
					
				case '(':
					charMatcher = ch -> ch == '(';
					break;
					
				case ')':
					charMatcher = ch -> ch == ')';
					break;
					
				case '|':
					charMatcher = ch -> ch == '|';
					break;
					
				default:
					throw new UnsupportedOperationException("Character '" + c2 + "' cannot be escaped here.");
				}
				regexMatchers.add(new SingleCharMatcher(charMatcher));
				break;
				
			case '+':
			{
				int lastIndex = regexMatchers.size() - 1;
				regexMatchers.set(lastIndex, new PlusMatcher(regexMatchers.get(lastIndex)));
				break;
			}

			case '?':
			{
				int lastIndex = regexMatchers.size() - 1;
				regexMatchers.set(lastIndex, new OptionalMatcher(regexMatchers.get(lastIndex)));
				break;
			}
				
			case '[':
				charMatcher = parseCharacterClass();
				regexMatchers.add(new SingleCharMatcher(charMatcher));
				break;
				
			case '(':
				// TODO clean this up.  It's a bit silly to call a method that
				// creates a RegularExpression object, then wrap it to make
				// it back into a RegexMatcher object.
				// In fact, what is the difference between RegularExpression
				// and RegexMatcher anyway.
				RegularExpression subExpression = parseRegularExpression(')');
				regexMatchers.add(new RegexMatcher() {
					@Override
					public boolean match(CharsFromInputLineReader reader) {
						return subExpression.match(reader);
					}
				});
				break;

			case '|':
				RegularExpression subExpression1 = new RegularExpression(regexMatchers);
				alternatives.add(subExpression1);
				regexMatchers = new ArrayList<>();
				break;

			default:
				// Anything else, it must be an exact match
				charMatcher = ch -> ch == c1;
				regexMatchers.add(new SingleCharMatcher(charMatcher));
				break;
			}
			
			c = query.charAt(i);
			i++;
		} while (c != terminatingChar && i < query.length());
		
		if (c != terminatingChar) {
			throw new TxrErrorOnLineException(startForErrorPurposes, i, "Regex not terminated on same line.  The terminating character of '" + terminatingChar + " is expected before the end of the line.");
		}

		if (alternatives.isEmpty()) {
			return new RegularExpression(regexMatchers);
		} else {
			RegularExpression subExpression1 = new RegularExpression(regexMatchers);
			alternatives.add(subExpression1);
			
			return new RegularExpression(
				Collections.singletonList(
				    new RegexMatcher() {
						@Override
						public boolean match(CharsFromInputLineReader reader) {
							for (RegularExpression alternative : alternatives) {
								if (alternative.match(reader)) {
									return true;
								};
							}
							return false;
						}
					}
				)
			);
		}
	}

	/**
	 * See section 6.13.  On entry, i will be positioned
	 * at the first character after the '[', being the first
	 * character of the single-character pattern.  On exit, i will be
	 * positioned at the first character after the terminating ']'.
	 * 
	 * @return an implementation of CharMatcher
	 */
	private CharMatcher parseCharacterClass() {
		List<CharMatcher> charMatchers = new ArrayList<>();
		
		char c = query.charAt(i);
		i++;
		
		boolean isNegated = false;
		if (c == '^') {
			isNegated = true;
			i++;
		}

		do {
			final char c1 = c;
			
			CharMatcher charMatcher;
			
			switch (c1) {
			case '\\':
				final char c2 = query.charAt(i);
				i++;

				switch (c2) {
				case 's':
					charMatcher = ch -> isWhiteSpace(ch);
					break;
					
				case 'w':
					charMatcher = ch -> isWordCharacter(ch);
					break;
					
				case 'd':
					charMatcher = ch -> isDigit(ch);
					break;
					
				case '\\':
					charMatcher = ch -> ch == '\\';
					break;
					
				case '^':
					charMatcher = ch -> ch == '^';
					break;
					
				case '-':
					charMatcher = ch -> ch == '-';
					break;
					
				case '[':
					charMatcher = ch -> ch == '[';
					break;
					
				case ']':
					charMatcher = ch -> ch == '[';
					break;
					
				default:
					throw new UnsupportedOperationException();
				}
				break;
				
			case '-':
				throw new UnsupportedOperationException("A '-' has been found in a character class. However the previous character was not a character that could be the start of the range.");

			default:
				// Anything else, it must be an exact match or range
				if (query.charAt(i) == '-') {
					i++;

					char cRangeEnd = query.charAt(i);
					i++;
					
					// It's a range
					if (c1 >= 'A' && c1 <= 'Z') {
						if (cRangeEnd < 'A' || cRangeEnd > 'Z') {
							throw new RuntimeException("The character class range is invalid.  If the range starts with an upper case letter then it must also end with an upper case letter.");
						}
						charMatcher = ch -> ch >= c1 && ch <= cRangeEnd;
					} else if (c1 >= 'a' && c1 <= 'z') {
						if (cRangeEnd < 'a' || cRangeEnd > 'z') {
							throw new RuntimeException("The character class range is invalid.  If the range starts with a lower case letter then it must also end with a lower case letter.");
						}
						charMatcher = ch -> ch >= c1 && ch <= cRangeEnd;
					} else if (c1 >= '0' && c1 <= '9') {
						if (cRangeEnd < '0' || cRangeEnd > '9') {
							throw new RuntimeException("The character class range is invalid.  If the range starts with a digit then it must also end with a digit.");
						}
						charMatcher = ch -> ch >= c1 && ch <= cRangeEnd;
					} else {
						throw new RuntimeException("A range cannot start with '" + c1 + "'.  Ranges can only be ranges of letters or digits.");
					}
				} else {
					// It's a single character
					charMatcher = ch -> ch == c1;
				}
				break;
			}
			
			charMatchers.add(charMatcher);

			c = query.charAt(i);
			i++;
		} while (c != ']' && i < query.length());
		
		if (c != ']') {
			throw new RuntimeException("Regex character class ([...]) not terminated on same line");
		}

		CharMatcher aggregateCharMatcher =
				ch -> orTogether(charMatchers, ch);
				
		if (isNegated) {
			return ch -> !aggregateCharMatcher.isMatch(ch);
		} else {
			return aggregateCharMatcher;
		}
	}
	
	private boolean orTogether(List<CharMatcher> charMatchers, char c) {
		return charMatchers.stream()
				.map(matcher -> matcher.isMatch(c))
				.reduce(false, (b1, b2) -> b1 || b2);
	}

	boolean isWordCharacter(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_';
	}

	boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	/**
	 * This may be a number or a lident.  A lident may
	 * consist of any combination of letters, digits,
	 * and ! $ % & * + - < = > ? \ _ ~ /
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
	 * and ! $ % & * + - < = > ? \ _ ~ /
	 * 
	 * A number
	 * 
	 * Note that a '.' is not a valid lident character.
	 * 
	 * @return
	 */
	private SubExpression parseLidentOrNumber() throws TxrErrorOnLineException {
		// First attempt to parse as a number
		// If that fails, see if it is a valid lident

		int start = i;

		if (query.charAt(i) == '+') {
			i++;
		} else if (query.charAt(i) == '-') {
			i++;
		}

		int startOfDigits = i;

		char c = query.charAt(i);
		while (Character.isDigit(c)) {
			i++;
			c = query.charAt(i);
		};

		if (c == '.') {
			do {
				i++;
				c = query.charAt(i);
			} while (Character.isDigit(c));

			boolean haveDigits = ((i - startOfDigits) >= 2);
			if (!haveDigits) {
				throw new RuntimeException("a '.' appears, but no digits before or after it so not a valid floating-point number.");
			}

			if (c == 'e' || c == 'E') {
				i++;
				if (query.charAt(i) == '+') {
					i++;
				} else if (query.charAt(i) == '-') {
					i++;
				}

				c = query.charAt(i);
				if (!Character.isDigit(c)) {
					// There's a '.', so can't be a lident.
					throw new RuntimeException("Invalid floating-point constant (and can't be a lident because it has a '.'.");
				}

				do {
					i++;
					c = query.charAt(i);
				} while (Character.isDigit(c));
			}

			if (isValidLidentChar(c)) {	
				// There's a '.', so can't be a lident.
				throw new RuntimeException("Invalid floating-point constant (and can't be a lident because it has a '.'.");
			}

			// It's a valid floating-point number
			try {
				return new FloatingPointLiteral(query.substring(start, i));
			} catch (TxrErrorException e) {
				throw new TxrErrorOnLineException(start, i, e.getMessage());
			}
		} else if (c == 'e' || c == 'E') {
			i++;
			if (query.charAt(i) == '+') {
				i++;
			} else if (query.charAt(i) == '-') {
				i++;
			}

			c = query.charAt(i);
			if (Character.isDigit(c)) {
				do {
					i++;
					c = query.charAt(i);
				} while (Character.isDigit(c));

				if (isValidLidentChar(c)) {	
					// Might still be a lident so process rest as a lident...
					do {
						i++;
						c = query.charAt(i);
					} while (isValidLidentChar(c));
					return new Symbol(query.substring(start, i));
				}

				// It's a valid floating-point number
				try {
					return new FloatingPointLiteral(query.substring(start, i));
				} catch (TxrErrorException e) {
					throw new TxrErrorOnLineException(start, i, e.getMessage());
				}

			} else {
				// It looked like a floating-point number, but no digit
				// followed the 'E'.  There was no '.' so we process this
				// as a lident.
				while (isValidLidentChar(c)) {
					i++;
					c = query.charAt(i);
				}
				return new Symbol(query.substring(start, i));
			}
		} else if (isValidLidentChar(c)) {
			// Possible integer following by lident (not digit),
			// can't be number so process as lident. 
			do {
				i++;
				c = query.charAt(i);
			} while (isValidLidentChar(c));
			return new Symbol(query.substring(start, i));
		} else {
			// If there's at least one digit, it's an integer.
			if (i - startOfDigits >= 1) {
				String integerAsText = query.substring(start, i);
				try {
					long value = Long.parseLong(integerAsText);
					return new IntegerLiteral(value);
				} catch (NumberFormatException e) {
					throw new RuntimeException("'" + integerAsText + "' appears to be a integer literal but it is out-of-range.");
				}
			} else {
				// It's a + or - only
				return new Symbol(query.substring(start, i));
			}
		}
	}

	private void appendThisLine() {
		ast.append(currentLine);
		currentLine = new Line();

	}

	private Ident parseSidentOrBident() throws TxrErrorOnLineException {
		if (query.charAt(i) == '{') {
			int start = i+1;

			char c;
			do {
				i++;
				c = query.charAt(i);
			} while (isValidBidentChar(c));

			String id = query.substring(start, i);
			Ident result = new Ident(id);
			
			if (isWhiteSpace(query.charAt(i))) {
				/*
				 * A bident may have a regular expression associated with it.
				 * Any regular expression will be separated from the bident
				 * itself by whitespace and will be inside the curly braces.
				 * See 6.8 of the manual.
				 */
				do {
					i++;
				} while (isWhiteSpace(query.charAt(i)));
			
				c = query.charAt(i);
				i++;
				if (c != '/') {
					throw new RuntimeException("Regular expression expected in bident but found: " + c);
				}
				
				RegularExpression regex = parseRegularExpression('/');
				result.setRegex(regex);
			}

			// TODO check to see if there can be whitespace here
			c = query.charAt(i);
			i++;
			if (c != '}') {
				throw new TxrErrorOnLineException(start-1, i-1, "Invalid character in bident: " + c);
			}
			
			return result;
		} else {
			return parseSident();
		}
	}

	private boolean isWhiteSpace(char c) {
		return c == ' ' || c == '\t';
	}

	private Ident parseSident() {
		int start = i;

		char c = query.charAt(i);
		do {
			i++;
			c = query.charAt(i);
		} while (isValidSidentChar(c));

		//		if (c != ' ' || c != ')') {
		//			throw new RuntimeException("Invalid character in sident: " + c);
		//		}
		return new Ident(query.substring(start,  i));
	}

	private boolean isValidSidentChar(char c) {
		return Character.isAlphabetic(c)
				|| Character.isDigit(c)
				|| c == '_';
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

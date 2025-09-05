import TokenType.*

class Scanner(private val source: String) {

    private val keywords: Map<String, TokenType> = mapOf(
        "and"    to AND,
        "class"  to CLASS,
        "else"   to ELSE,
        "false"  to FALSE,
        "for"    to FOR,
        "fun"    to FUN,
        "if"     to IF,
        "nil"    to NIL,
        "or"     to OR,
        "print"  to PRINT,
        "return" to RETURN,
        "super"  to SUPER,
        "this"   to THIS,
        "true"   to TRUE,
        "var"    to VAR,
        "while"  to WHILE,
    )

    private val tokens = mutableListOf<Token>()

    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): MutableList<Token> {
        while (!isAtEnd) {
            // We are at the beginning of the next lexeme.
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> if (match('/')) {
                // A comment goes until the end of the line.
                while (peek() != '\n' && !this.isAtEnd) advance()
            } else {
                addToken(SLASH)
            }

            ' ', '\r', '\t' -> {}
            '\n' -> line++
            '"' -> string()
            else ->
                if (isDigit(c)) number()
                else if (isAlpha(c)) identifier()
                else Lox.error(line, "Unexpected character: $c")
        }
    }

    private fun isAlpha(c: Char): Boolean {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') || c == '_'
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start, current)
        val type = keywords.getOrDefault(text, IDENTIFIER)
        addToken(type)
    }

    private fun number() {
        while (isDigit(peek())) advance()
        if (peek() == '.' && isDigit(peekNext())) {
            advance()
            while (isDigit(peek())) advance()
        }
        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source.get(current + 1)
    }

    private fun isDigit(c: Char): Boolean {
        return c >= '0' && c <= '9'
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd) {
            if (peek() == '\n') line++
            advance()
        }
        if (isAtEnd) {
            Lox.error(line, "Unterminated string.")
            return
        }
        advance()
        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun peek(): Char {
        if (this.isAtEnd) return '\u0000'
        return source[current]
    }

    private fun match(expected: Char): Boolean {
        if (this.isAtEnd) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private val isAtEnd: Boolean
        get() = current >= source.length

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

}
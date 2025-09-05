import TokenType.*

class Scanner(private val source: String) {
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

            else -> Lox.error(line, "Unexpected character.");
        }
    }

    private fun match(expected: Char): Boolean {
        if (this.isAtEnd) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun peek(): Char {
        if (this.isAtEnd) return '\u0000'
        return source.get(current)
    }

    private val isAtEnd: Boolean
        get() = current >= source.length

    private fun advance(): Char {
        return source.get(current++)
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    companion object Qwe {

        private val keywords = mutableMapOf<String, TokenType>()

        init {
            keywords.put("and", AND)
            keywords.put("class", CLASS)
            keywords.put("else", ELSE)
            keywords.put("false", FALSE)
            keywords.put("for", FOR)
            keywords.put("fun", FUN)
            keywords.put("if", IF)
            keywords.put("nil", NIL)
            keywords.put("or", OR)
            keywords.put("print", PRINT)
            keywords.put("return", RETURN)
            keywords.put("super", SUPER)
            keywords.put("this", THIS)
            keywords.put("true", TRUE)
            keywords.put("var", VAR)
            keywords.put("while", WHILE)
        }
    }
}
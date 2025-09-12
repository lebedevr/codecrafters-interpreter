import Expr.Assign
import Expr.Logical
import Stmt.While
import TokenType.*
import java.util.*

internal class Parser(tokens: MutableList<Token>) {
    private class ParseError : RuntimeException()

    private val tokens: MutableList<Token>
    private var current = 0

    var allowExpression = false
    var foundExpression = false

    init {
        this.tokens = tokens
    }

    fun parseRepl(): Any? {
        allowExpression = true
        val statements: MutableList<Stmt?> = ArrayList<Stmt?>()
        while (!isAtEnd) {
            statements.add(declaration())

            if (foundExpression) {
                val last = statements.get(statements.size - 1)
                return (last as Stmt.Expression).expression
            }

            allowExpression = false
        }

        return statements
    }

    fun parse(): MutableList<Stmt?> {
        val statements: MutableList<Stmt?> = ArrayList()
        while (!this.isAtEnd) {
            statements.add(declaration())
        }

        return statements // [parse-error-handling]
    }

    private fun expression(): Expr? {
        return assignment()
    }

    private fun declaration(): Stmt? {
        try {
            if (match(VAR)) return varDeclaration()
            return statement()
        } catch (error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun statement(): Stmt {
        if (match(FOR)) return forStatement()
        if (match(IF)) return ifStatement()
        if (match(PRINT)) return printStatement()
        if (match(WHILE)) return whileStatement()
        if (match(LEFT_BRACE)) return Stmt.Block(block())

        return expressionStatement()
    }

    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'for'.")
        val initializer: Stmt?
        if (match(SEMICOLON)) {
            initializer = null
        } else if (match(VAR)) {
            initializer = varDeclaration()
        } else {
            initializer = expressionStatement()
        }

        var condition: Expr? = null
        if (!check(SEMICOLON)) {
            condition = expression()
        }
        consume(SEMICOLON, "Expect ';' after loop condition.")

        var increment: Expr? = null
        if (!check(RIGHT_PAREN)) {
            increment = expression()
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.")

        var body: Stmt? = statement()

        if (increment != null) {
            body = Stmt.Block(
                Arrays.asList<Stmt?>(
                    body,
                    Stmt.Expression(increment)
                )
            )
        }

        if (condition == null) condition = Expr.Literal(true)
        body = While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(Arrays.asList<Stmt?>(initializer, body))
        }

        return body
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition: Expr = expression()!!
        consume(RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        var elseBranch: Stmt? = null
        if (match(ELSE)) {
            elseBranch = statement()
        }

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Stmt {
        val value: Expr? = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")
        val body: Stmt = statement()

        return While(condition, body)
    }

    private fun varDeclaration(): Stmt {
        val name: Token? = consume(IDENTIFIER, "Expect variable name.")

        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun expressionStatement(): Stmt {
        val expr: Expr? = expression()
        if (allowExpression && isAtEnd) {
            foundExpression = true;
        } else {
            consume(SEMICOLON, "Expect ';' after expression.")
        }
        return Stmt.Expression(expr)
    }

    private fun block(): MutableList<Stmt?> {
        val statements: MutableList<Stmt?> = ArrayList<Stmt?>()

        while (!check(RIGHT_BRACE) && !this.isAtEnd) {
            statements.add(declaration())
        }

        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun assignment(): Expr? {
        val expr: Expr? = or()

        if (match(EQUAL)) {
            val equals: Token? = previous()
            val value: Expr? = assignment()

            if (expr is Expr.Variable) {
                val name: Token? = expr.name
                return Assign(name, value)
            }

            error(equals, "Invalid assignment target.") // [no-throw]
        }

        return expr
    }

    private fun or(): Expr? {
        var expr: Expr? = and()

        while (match(OR)) {
            val operator = previous()
            val right: Expr? = and()
            expr = Logical(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expr? {
        var expr: Expr? = equality()

        while (match(AND)) {
            val operator = previous()
            val right: Expr? = equality()
            expr = Logical(expr, operator, right)
        }

        return expr
    }

    private fun equality(): Expr {
        var expr: Expr = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator: Token? = previous()
            val right: Expr = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr: Expr = term()

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator: Token? = previous()
            val right: Expr = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr: Expr = factor()

        while (match(MINUS, PLUS)) {
            val operator: Token? = previous()
            val right: Expr = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr: Expr = unary()

        while (match(SLASH, STAR)) {
            val operator: Token? = previous()
            val right: Expr = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator: Token? = previous()
            val right: Expr = unary()
            return Expr.Unary(operator, right)
        }

        return primary()
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)

        if (match(NUMBER, STRING)) {
            return Expr.Literal(previous()?.literal)
        }

        if (match(IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(LEFT_PAREN)) {
            val expr: Expr? = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expect expression.")
    }

    private fun match(vararg types: TokenType?): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun consume(type: TokenType?, message: String?): Token? {
        if (check(type)) return advance()

        throw error(peek(), message)
    }

    private fun check(type: TokenType?): Boolean {
        if (this.isAtEnd) return false
        return peek()?.type === type
    }

    private fun advance(): Token? {
        if (!this.isAtEnd) current++
        return previous()
    }

    private val isAtEnd: Boolean
        get() = peek()?.type === EOF

    private fun peek(): Token? {
        return tokens.get(current)
    }

    private fun previous(): Token? {
        return tokens.get(current - 1)
    }

    private fun error(token: Token?, message: String?): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!this.isAtEnd) {
            if (previous()?.type === SEMICOLON) return

            when (peek()?.type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> {}
            }

            advance()
        }
    }
}
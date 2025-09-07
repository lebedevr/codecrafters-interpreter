import Expr.Assign

internal class AstPrinter : Expr.Visitor<String?>, Stmt.Visitor<String?> {
    fun print(expr: Expr): String? {
        return expr.accept<String?>(this)
    }

    fun print(stmt: Stmt): String? {
        return stmt.accept<String?>(this)
    }

    override fun visitBlockStmt(stmt: Stmt.Block): String {
        val builder = StringBuilder()
        builder.append("(block ")

        for (statement in stmt.statements) {
            builder.append(statement?.accept<String?>(this))
        }

        builder.append(")")
        return builder.toString()
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): String {
        return parenthesize(";", stmt.expression!!)
    }

    override fun visitPrintStmt(stmt: Stmt.Print): String {
        return parenthesize("print", stmt.expression!!)
    }

    override fun visitVarStmt(stmt: Stmt.Var): String {
        if (stmt.initializer == null) {
            return parenthesize2("var", stmt.name)
        }

        return parenthesize2("var", stmt.name, "=", stmt.initializer)
    }

    override fun visitAssignExpr(expr: Assign): String {
        return parenthesize2("=", expr.name!!.lexeme, expr.value)
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        return parenthesize(
            expr.operator!!.lexeme,
            expr.left!!, expr.right!!
        )
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return parenthesize("group", expr.expression!!)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String? {
        if (expr.value == null) return "nil"
        return expr.value.toString()
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return parenthesize(expr.operator!!.lexeme, expr.right!!)
    }

    override fun visitVariableExpr(expr: Expr.Variable): String? {
        return expr.name!!.lexeme
    }

    private fun parenthesize(name: String?, vararg exprs: Expr): String {
        val builder = StringBuilder()

        builder.append("(").append(name)
        for (expr in exprs) {
            builder.append(" ")
            builder.append(expr.accept<String?>(this))
        }
        builder.append(")")

        return builder.toString()
    }

    // Note: AstPrinting other types of syntax trees is not shown in the
    // book, but this is provided here as a reference for those reading
    // the full code.
    private fun parenthesize2(name: String?, vararg parts: Any?): String {
        val builder = StringBuilder()

        builder.append("(").append(name)
        transform(builder, *parts)
        builder.append(")")

        return builder.toString()
    }

    private fun transform(builder: StringBuilder, vararg parts: Any?) {
        for (part in parts) {
            builder.append(" ")
            if (part is Expr) {
                builder.append(part.accept<String?>(this))
            } else if (part is Stmt) {
                builder.append(part.accept<String?>(this))
            } else if (part is Token) {
                builder.append(part.lexeme)
            } else if (part is MutableList<*>) {
                transform(builder, *part.toTypedArray())
            } else {
                builder.append(part)
            }
        }
    }
}

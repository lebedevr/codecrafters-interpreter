abstract class Stmt {
    interface Visitor<R> {
        fun visitBlockStmt(stmt: Block): R?
        fun visitClassStmt(stmt: Class): R?
        fun visitExpressionStmt(stmt: Expression): R?
        fun visitFunctionStmt(stmt: Function): R?
        fun visitIfStmt(stmt: If): R?
        fun visitPrintStmt(stmt: Print): R?
        fun visitReturnStmt(stmt: Return): R?
        fun visitVarStmt(stmt: Var): R?
        fun visitWhileStmt(stmt: While): R?
    }

    abstract fun <R> accept(visitor: Visitor<R?>): R?

    // Nested Stmt classes here...
    //> stmt-block
    class Block(val statements: MutableList<Stmt?>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitBlockStmt(this)
        }
    }

    class Class (
        val name: Token,
        val methods: MutableList<Function>?
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitClassStmt(this)
        }
    }

    //< stmt-block
    //> stmt-expression
    class Expression(val expression: Expr?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitExpressionStmt(this)
        }
    }

    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitIfStmt(this)
        }
    }

    class Function(
        val name: Token?,
        val params: MutableList<Token?>,
        val body: MutableList<Stmt?>
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitFunctionStmt(this)
        }
    }

    //< stmt-expression
    //> stmt-print
    class Print(val expression: Expr?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitPrintStmt(this)
        }
    }

    class Return(val keyword: Token, val value: Expr?) : Stmt() {
        override fun <R> accept(visitor: Stmt.Visitor<R?>): R? {
            return visitor.visitReturnStmt(this)
        }
    }

    //< stmt-print
    //> stmt-var
    class Var(val name: Token?, val initializer: Expr?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitVarStmt(this)
        }
    }

    //< stmt-var

    class While (val condition: Expr?, val body: Stmt?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitWhileStmt(this)
        }
    }

} //< Appendix II stmt

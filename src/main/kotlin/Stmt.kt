abstract class Stmt {
    interface Visitor<R> {
        fun visitBlockStmt(stmt: Block): R?
        fun visitExpressionStmt(stmt: Expression): R?
        fun visitPrintStmt(stmt: Print): R?
        fun visitVarStmt(stmt: Var): R?
    }

    abstract fun <R> accept(visitor: Visitor<R?>): R?

    // Nested Stmt classes here...
    //> stmt-block
    class Block(val statements: MutableList<Stmt?>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitBlockStmt(this)
        }
    }

    //< stmt-block
    //> stmt-expression
    class Expression(val expression: Expr?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitExpressionStmt(this)
        }
    }

    //< stmt-expression
    //> stmt-print
    class Print(val expression: Expr?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitPrintStmt(this)
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

} //< Appendix II stmt

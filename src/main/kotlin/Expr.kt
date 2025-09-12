//> Appendix II expr
 abstract class Expr {
     interface Visitor<R> {
        fun visitAssignExpr(expr: Assign): R?
        fun visitBinaryExpr(expr: Binary): R?
        fun visitGroupingExpr(expr: Grouping): R?
        fun visitLiteralExpr(expr: Literal): R?
        fun visitLogicalExpr(expr: Logical): R?
        fun visitUnaryExpr(expr: Unary): R?
        fun visitVariableExpr(expr: Variable): R?
    }

    //< expr-variable
    abstract fun <R> accept(visitor: Visitor<R?>): R?
    // Nested Expr classes here...
    //> expr-assign
     class Assign(@JvmField val name: Token?, @JvmField val value: Expr?) : Expr() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitAssignExpr(this)
        }
    }

    //< expr-assign
    //> expr-binary
     class Binary(@JvmField val left: Expr?, @JvmField val operator: Token?, @JvmField val right: Expr?) : Expr() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitBinaryExpr(this)
        }
    }

    //< expr-binary
    //> expr-grouping
     class Grouping(@JvmField val expression: Expr?) : Expr() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitGroupingExpr(this)
        }
    }

    //< expr-grouping
    //> expr-literal
     class Literal(@JvmField val value: Any?) : Expr() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitLiteralExpr(this)
        }
    }

    class Logical (val left: Expr?, val operator: Token?, val right: Expr?) : Expr() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitLogicalExpr(this)
        }
    }

    //< expr-literal
    //> expr-unary
     class Unary(@JvmField val operator: Token?, @JvmField val right: Expr?) : Expr() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitUnaryExpr(this)
        }
    }

    //< expr-unary
    //> expr-variable
     class Variable(@JvmField val name: Token?) : Expr() {
        override fun <R> accept(visitor: Visitor<R?>): R? {
            return visitor.visitVariableExpr(this)
        }
    }

} //< Appendix II expr

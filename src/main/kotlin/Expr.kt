//> Appendix II expr
abstract class Expr {
     interface Visitor<R> {
        fun visitBinaryExpr(expr: Binary): R?
        fun visitGroupingExpr(expr: Grouping): R?
        fun visitLiteralExpr(expr: Literal): R?
        fun visitUnaryExpr(expr: Unary): R?
    }

    // Nested Expr classes here...
    //> expr-binary
     class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R? {
            return visitor.visitBinaryExpr(this)
        }
    }

    //< expr-binary
    //> expr-grouping
     class Grouping(val expression: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R? {
            return visitor.visitGroupingExpr(this)
        }
    }

    //< expr-grouping
    //> expr-literal
     class Literal(val value: Any?) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R? {
            return visitor.visitLiteralExpr(this)
        }
    }

    //< expr-literal
    //> expr-unary
     class Unary(val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R? {
            return visitor.visitUnaryExpr(this)
        }
    }

    //< expr-unary
    abstract fun <R> accept(visitor: Visitor<R>): R?
} //< Appendix II expr

import Expr.Assign
import Lox.runtimeError


class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Void?> {

    val globals: Environment = Environment()
    private var environment = globals
    private val locals: MutableMap<Expr, Int> = HashMap()

    init {
        globals.define("clock", object : LoxCallable {
            override fun arity(): Int {
                return 0
            }

            override fun call(interpreter: Interpreter, arguments: MutableList<Any?>): Any? {
                return System.currentTimeMillis().toDouble() / 1000.0
            }

            override fun toString(): String {
                return "<native fn>"
            }
        })
    }

    fun interpret(statements: MutableList<Stmt?>) {
        try {
            for (statement in statements) {
                statement?.let { execute(it) }
            }
        } catch (error: RuntimeError) {
            runtimeError(error)
        }
    }

     fun interpret(expression: Expr?): String? {
         try {
             val value = evaluate(expression!!)
             return stringify(value)
         } catch (error: RuntimeError) {
             runtimeError(error)
             return null
         }
     }

    fun resolve(expr: Expr, depth: Int) {
        locals.put(expr, depth)
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept<Any?>(this)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept<Void?>(this)
    }

    fun executeBlock(
        statements: MutableList<Stmt?>,
        environment: Environment
    ) {
        val previous = this.environment
        try {
            this.environment = environment

            for (statement in statements) {
                statement?.let { execute(it) }
            }
        } finally {
            this.environment = previous
        }
    }

    override fun visitBlockStmt(stmt: Stmt.Block): Void? {
        executeBlock(stmt.statements, Environment(environment))
        return null
    }

    override fun visitClassStmt(stmt: Stmt.Class): Void? {
        environment.define(stmt.name.lexeme, null)

        val methods: MutableMap<String, LoxFunction> = HashMap()
        for (method in stmt.methods) {
            val function = LoxFunction(method, environment)
            methods.put(method.name!!.lexeme, function)
        }

        val klass = LoxClass(stmt.name.lexeme, methods)
        environment.assign(stmt.name, klass)
        return null
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): Void? {
        evaluate(stmt.expression!!)
        return null
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): Void? {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name!!.lexeme, function)
        return null
    }

     override fun visitIfStmt(stmt: Stmt.If): Void? {
         if (isTruthy(evaluate(stmt.condition))) {
             execute(stmt.thenBranch)
         } else if (stmt.elseBranch != null) {
             execute(stmt.elseBranch)
         }
         return null;
     }

    override fun visitPrintStmt(stmt: Stmt.Print): Void? {
        val value = evaluate(stmt.expression!!)
        println(stringify(value))
        return null
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Void? {
        var value: Any? = null
        if (stmt.value != null) value = evaluate(stmt.value)

        throw Return(value)
    }

    override fun visitVarStmt(stmt: Stmt.Var): Void? {
        var value: Any? = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }

        environment.define(stmt.name!!.lexeme, value)
        return null
    }

     override fun visitWhileStmt(stmt: Stmt.While): Void? {
         while (isTruthy(evaluate(stmt.condition!!))) {
             execute(stmt.body!!);
         }
         return null;
     }

     override fun visitAssignExpr(expr: Assign): Any? {
        val value = evaluate(expr.value!!)
         val distance = locals.get(expr)
         if (distance != null) {
             environment.assignAt(distance, expr.name!!, value)
         } else {
             globals.assign(expr.name!!, value)
         }
        return value
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left!!)
        val right = evaluate(expr.right!!) // [left]

        when (expr.operator!!.type) {
            TokenType.BANG_EQUAL -> return !isEqual(left, right)
            TokenType.EQUAL_EQUAL -> return isEqual(left, right)
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double > right as Double
            }

            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double >= right as Double
            }

            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) < (right as Double)
            }

            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double <= right as Double
            }

            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double - right as Double
            }

            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    return left + right
                } // [plus]


                if (left is String && right is String) {
                    return left + right
                }

                throw RuntimeError(
                    expr.operator,
                    "Operands must be two numbers or two strings."
                )
            }

            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double / right as Double
            }

            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double * right as Double
            }
            else -> {}
        }

        // Unreachable.
        return null
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee!!)

        val arguments: MutableList<Any?> = ArrayList<Any?>()
        for (argument in expr.arguments!!) {
            arguments.add(evaluate(argument!!))
        }

        if (callee !is LoxCallable) {
            throw RuntimeError(
                expr.paren!!,
                "Can only call functions and classes."
            )
        }

        val function: LoxCallable = callee as LoxCallable

        if (arguments.size != function.arity()) {
            throw RuntimeError(
                expr.paren!!, "Expected " +
                        function.arity() + " arguments but got " +
                        arguments.size + "."
            )
        }
        return function.call(this, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val `object` = evaluate(expr.`object`!!)
        if (`object` is LoxInstance) {
            return `object`.get(expr.name!!)
        }

        throw RuntimeError(
            expr.name!!,
            "Only instances have properties."
        )
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression!!)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

     override fun visitLogicalExpr(expr: Expr.Logical): Any? {
         val left = evaluate(expr.left!!)

         if (expr.operator!!.type === TokenType.OR) {
             if (isTruthy(left)) return left
         } else {
             if (!isTruthy(left)) return left
         }

         return evaluate(expr.right!!)
     }

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val `object` = evaluate(expr.`object`!!)

        if (`object` !is LoxInstance) {
            throw RuntimeError(
                expr.name!!,
                "Only instances have fields."
            )
        }

        val value = evaluate(expr.value!!)
        `object`.set(expr.name!!, value)
        return value
    }

    override fun visitThisExpr(expr: Expr.This): Any? {
        return lookUpVariable(expr.keyword!!, expr)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right!!)

        when (expr.operator!!.type) {
            TokenType.BANG -> return !isTruthy(right)
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                return -(right as Double)
            }
            else -> {}
        }

        // Unreachable.
        return null
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return lookUpVariable(expr.name!!, expr)
    }

    private fun lookUpVariable(name: Token, expr: Expr?): Any? {
        val distance = locals.get(expr)
        if (distance != null) {
            return environment.getAt(distance, name.lexeme)
        } else {
            return globals.get(name)
        }
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(
        operator: Token,
        left: Any?, right: Any?
    ) {
        if (left is Double && right is Double) return
        // [operand]
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun isTruthy(`object`: Any?): Boolean {
        if (`object` == null) return false
        if (`object` is Boolean) return `object`
        return true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null) return false

        return a == b
    }

    private fun stringify(`object`: Any?): String? {
        if (`object` == null) return "nil"

        if (`object` is Double) {
            var text = `object`.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }

        return `object`.toString()
    }
}

import Expr.Assign
import Expr.Logical
import Stmt.While
import java.util.*


class Resolver(private val interpreter: Interpreter) : Expr.Visitor<Void?>, Stmt.Visitor<Void?> {
    private val scopes = Stack<MutableMap<String?, Boolean?>>()
    private var currentFunction: FunctionType = FunctionType.NONE

    private enum class FunctionType {
        NONE,
        FUNCTION,
        METHOD
    }

    fun resolve(statements: MutableList<Stmt?>) {
        for (statement in statements) {
            resolve(statement)
        }
    }

    override fun visitBlockStmt(stmt: Stmt.Block): Void? {
        beginScope()
        resolve(stmt.statements)
        endScope()
        return null
    }

    override fun visitClassStmt(stmt: Stmt.Class): Void? {
        declare(stmt.name)
        define(stmt.name)

        for (method in stmt.methods) {
            val declaration: FunctionType = FunctionType.METHOD
            resolveFunction(method, declaration)
        }

        return null
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): Void? {
        resolve(stmt.expression!!)
        return null
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): Void? {
        declare(stmt.name!!)
        define(stmt.name)

        resolveFunction(stmt, FunctionType.FUNCTION)
        return null
    }

    override fun visitIfStmt(stmt: Stmt.If): Void? {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if (stmt.elseBranch != null) resolve(stmt.elseBranch)
        return null
    }

    override fun visitPrintStmt(stmt: Stmt.Print): Void? {
        resolve(stmt.expression!!)
        return null
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Void? {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.")
        }

        if (stmt.value != null) {
            resolve(stmt.value)
        }

        return null
    }

    override fun visitVarStmt(stmt: Stmt.Var): Void? {
        declare(stmt.name!!)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
        return null
    }

    override fun visitWhileStmt(stmt: While): Void? {
        resolve(stmt.condition!!)
        resolve(stmt.body!!)
        return null
    }

    override fun visitAssignExpr(expr: Assign): Void? {
        resolve(expr.value!!)
        resolveLocal(expr, expr.name!!)
        return null
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Void? {
        resolve(expr.left!!)
        resolve(expr.right!!)
        return null
    }

    override fun visitCallExpr(expr: Expr.Call): Void? {
        resolve(expr.callee!!)

        for (argument in expr.arguments!!) {
            resolve(argument!!)
        }

        return null
    }

    override fun visitGetExpr(expr: Expr.Get): Void? {
        resolve(expr.`object`!!)
        return null
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Void? {
        resolve(expr.expression!!)
        return null
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Void? {
        return null
    }

    override fun visitLogicalExpr(expr: Logical): Void? {
        resolve(expr.left!!)
        resolve(expr.right!!)
        return null
    }

    override fun visitSetExpr(expr: Expr.Set): Void? {
        resolve(expr.value!!)
        resolve(expr.`object`!!)
        return null
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Void? {
        resolve(expr.right!!)
        return null
    }

    override fun visitVariableExpr(expr: Expr.Variable): Void? {
        if (!scopes.isEmpty() &&
            scopes.peek().get(expr.name!!.lexeme) === java.lang.Boolean.FALSE
        ) {
            Lox.error(
                expr.name,
                "Can't read local variable in its own initializer."
            )
        }

        resolveLocal(expr, expr.name!!)
        return null
    }

    private fun resolve(stmt: Stmt?) {
        stmt?.accept(this)
    }

    private fun resolve(expr: Expr) {
        expr.accept(this)
    }

    private fun resolveFunction(
        function: Stmt.Function, type: FunctionType
    ) {
        val enclosingFunction = currentFunction
        currentFunction = type

        beginScope()
        for (param in function.params) {
            declare(param!!)
            define(param)
        }
        resolve(function.body)
        endScope()
        currentFunction = enclosingFunction
    }

    private fun beginScope() {
        scopes.push(HashMap<String?, Boolean?>())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return

        val scope = scopes.peek()
        if (scope.containsKey(name.lexeme)) {
            Lox.error(
                name,
                "Already a variable with this name in this scope."
            )
        }

        scope.put(name.lexeme, false)
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.peek().put(name.lexeme, true)
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in scopes.indices.reversed()) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }
}

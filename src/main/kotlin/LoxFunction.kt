class LoxFunction(val declaration: Stmt.Function) : LoxCallable {

    override fun arity(): Int = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: MutableList<Any?>): Any? {
        val environment = Environment(interpreter.globals)

        for (i in declaration.params.indices) {
            environment.define(
                declaration.params[i]!!.lexeme,
                arguments[i]
            )
        }

        interpreter.executeBlock(declaration.body, environment)
        return null
    }

    override fun toString(): String {
        return "<fn " + declaration.name!!.lexeme + ">"
    }

}
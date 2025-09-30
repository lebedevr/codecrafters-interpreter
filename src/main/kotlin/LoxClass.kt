class LoxClass(val name: String) : LoxCallable {
    override fun toString(): String {
        return name
    }

    override fun arity(): Int {
        return 0
    }

    override fun call(interpreter: Interpreter, arguments: MutableList<Any?>): Any? {
        val instance: LoxInstance = LoxInstance(this)
        return instance
    }
}
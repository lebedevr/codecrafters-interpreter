class LoxClass(val name: String, private val methods: Map<String, LoxFunction>) : LoxCallable {
    override fun toString(): String {
        return name
    }

    override fun arity(): Int {
        return 0
    }

    override fun call(interpreter: Interpreter, arguments: MutableList<Any?>): Any? {
        val instance = LoxInstance(this)
        return instance
    }

    fun findMethod(name: String): LoxFunction? {
        if (methods.containsKey(name)) {
            return methods[name]
        }

        return null
    }
}
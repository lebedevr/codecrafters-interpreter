class LoxClass(val name: String, val superclass: LoxClass?, private val methods: Map<String, LoxFunction>) : LoxCallable {
    override fun toString(): String {
        return name
    }

    override fun arity(): Int {
        val initializer = findMethod("init") ?: return 0
        return initializer.arity()
    }

    override fun call(interpreter: Interpreter, arguments: MutableList<Any?>): Any? {
        val instance = LoxInstance(this)
        val initializer = findMethod("init")
        initializer?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    fun findMethod(name: String): LoxFunction? {
        if (methods.containsKey(name)) {
            return methods[name]
        }

        return null
    }
}
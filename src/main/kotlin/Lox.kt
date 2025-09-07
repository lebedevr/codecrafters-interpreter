import Lox.runFile
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {


    val command = args[0]
    val filename = args[1]
    if (command !in listOf("tokenize", "parse", "evaluate", "run")) {
        System.err.println("Unknown command: ${command}")
        exitProcess(1)
    }
    runFile(filename, command)
}

object Lox {
    private val interpreter: Interpreter = Interpreter()
    var hadError: Boolean = false
    var hadRuntimeError: Boolean = false

    @Throws(IOException::class)
    fun runFile(path: String, command: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()), command)

        // Indicate an error in the exit code.
        if (hadError) System.exit(65)
        if (hadRuntimeError) System.exit(70)
    }

    fun run(source: String, command: String) {
        val scanner: Scanner = Scanner(source)
        val tokens: MutableList<Token> = scanner.scanTokens()
        if (command == "tokenize") {
            for (token in tokens) {
                println(token)
            }
        } else if (command == "parse") {
            val parser = Parser(tokens)
            val expression = parser.parse()
            if (hadError) return
            val syntax = parser.parseRepl()
            if (syntax is Expr) {
                val result = interpreter.interpret(syntax)
                if (result != null) {
                    println("= " + result)
                }
            }
        } else if (command == "evaluate") {
            val parser = Parser(tokens)
            val syntax = parser.parseRepl()
            if (syntax is MutableList<*>) {
                interpreter.interpret(syntax as MutableList<Stmt?>)
            } else if (syntax is Expr) {
                val result = interpreter.interpret(syntax)
                if (result != null) {
                    println(result)
                }
            }
        } else if (command == "run") {

            val parser = Parser(tokens)
            val statements: MutableList<Stmt?> = parser.parse()
            // Stop if there was a syntax error.
            if (hadError) return

            interpreter.interpret(statements)
        }
    }

    fun error(line: Int, message: String?) {
        report(line, "", message)
    }

    fun report(
        line: Int, where: String?,
        message: String?
    ) {
        System.err.println(
            "[line " + line + "] Error" + where + ": " + message
        )
        hadError = true
    }

    fun error(token: Token?, message: String?) {
        if (token?.type === TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token!!.line, " at '" + token?.lexeme + "'", message)
        }
    }

    @JvmStatic
    fun runtimeError(error: RuntimeError) {
        System.err.println(
            error.message +
                    "\n[line " + error.token.line + "]"
        )
        hadRuntimeError = true
    }
}
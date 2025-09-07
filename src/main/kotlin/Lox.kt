import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

object Lox {
    private val interpreter: Interpreter = Interpreter()
    var hadError: Boolean = false
    var hadRuntimeError: Boolean = false

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size > 1) {
            println("Usage: jlox [script]")
            System.exit(64) // [64]
        } else if (args.size == 1) {
            runFile(args[0])
        } else {
            runPrompt()
        }
    }

    @Throws(IOException::class)
    private fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))

        // Indicate an error in the exit code.
        if (hadError) System.exit(65)
        if (hadRuntimeError) System.exit(70)
    }

    @Throws(IOException::class)
    private fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            // [repl]
            print("> ")
            val line = reader.readLine()
            if (line == null) break
            run(line)
            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner: Scanner = Scanner(source)
        val tokens: MutableList<Token> = scanner.scanTokens()
        val parser: Parser = Parser(tokens)
        val statements: MutableList<Stmt?> = parser.parse()

        // Stop if there was a syntax error.
        if (hadError) return

        interpreter.interpret(statements)
    }

    fun error(line: Int, message: String?) {
        report(line, "", message)
    }

    private fun report(
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
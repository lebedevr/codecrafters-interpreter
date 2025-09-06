import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    Lox.main(args)
}

object Lox {

    var hadError: Boolean = false

    var hadRuntimeError: Boolean = false

    fun main(args: Array<String>) {
        if (args.size < 2) {
            System.err.println("Usage: ./your_program.sh tokenize <filename>")
            exitProcess(1)
        }
        val command = args[0]
        val filename = args[1]
        if (command !in listOf("tokenize", "parse")) {
            System.err.println("Unknown command: ${command}")
            exitProcess(1)
        }
        runFile(filename, command)
    }

    private fun runFile(path: String, command: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()), command)
        if (hadError) System.exit(65)
        if (hadRuntimeError) System.exit(70)
    }

    private fun run(source: String, command: String) {
        val scanner: Scanner = Scanner(source)
        val tokens: MutableList<Token> = scanner.scanTokens()
        if (command == "tokenize") {
            for (token in tokens) {
                println(token)
            }
        } else if (command == "parse") {
            val parser = Parser(tokens)
            val expression = parser.parse()

            // Stop if there was a syntax error.
            if (hadError) return

            expression?.also { println(AstPrinter().print(expression)) }

        }

    }


    fun error(line: Int, message: String?) {
        report(line, "", message)
    }

    fun error(token: Token, message: String?) {
        if (token.type === TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '" + token.lexeme + "'", message)
        }
    }

    fun runtimeError(error: RuntimeError) {
        System.err.println(error.message + "\n[line " + error.token.line + "]")
        hadRuntimeError = true
    }

    private fun report(
        line: Int, where: String?,
        message: String?
    ) {
        System.err.println("[line " + line + "] Error" + where + ": " + message)
        hadError = true
    }

}
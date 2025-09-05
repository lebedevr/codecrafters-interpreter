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
        if (command != "tokenize") {
            System.err.println("Unknown command: ${command}")
            exitProcess(1)
        }
        runFile(filename)
    }

    private fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
        if (hadError) System.exit(65)
        if (hadRuntimeError) System.exit(70)
    }

    private fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
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

        for (token in tokens) {
            println(token)
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
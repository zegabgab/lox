package jlox;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class Lox {
    private static boolean hadError = false;
    private static final ExprVisitor<?> visitor = new Evaluator();

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: jlox [sourcefile]");
            System.exit(69);
        } else if (args.length == 1) {
            try {
                System.exit(runFile(args[0]));
            } catch (IOException e) {
                System.err.println("Error running from file: " + e.getLocalizedMessage());
                System.exit(68);
            }
        } else {
            try {
                runPrompt();
            } catch (IOException e) {
                System.err.println("Error running interactive prompt: " + e.getLocalizedMessage());
                System.exit(67);
            }
        }
    }

    private static int runFile(String path) throws IOException {
        try (FileInputStream stream = new FileInputStream(path)) {
            String source = new String(stream.readAllBytes(), Charset.defaultCharset());
            run(source);
        }
        return hadError ? 66 : 0;
    }

    private static void runPrompt() throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in)
        );

        for (;;) {
            System.out.print(">| ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        if (hadError) {
            return;
        }
        Parser parser = new Parser(tokens);
        var expression = parser.parse();

        expression.ifPresentOrElse(
                expr -> System.out.println(expr.accept(visitor)),
                () -> System.out.println("This was wrong")
        );
    }

    static void error(int lineNo, String message) {
        report(lineNo, "", message);
    }

    static void error(Token token, String message) {
        final String where = token.type.equals(TokenType.EOF)
                ? " at end"
                : " at '" + token.lexeme + '\'';
        report(token.lineNo, where, message);
    }

    private static void report(int lineNo, String where, String message) {
        System.err.println("[line " + lineNo + "] Error" + where + ": " + message);
        hadError = true;
    }
}

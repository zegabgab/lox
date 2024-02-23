package jlox;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.stream.*;

public class Lox {
    private static boolean hadError = false;

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: jlox [sourcefile]");
            System.exit(69);
        } else if (args.length == 1) {
            try {
                runFile(args[0]);
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

    private static void runFile(String path) throws IOException {
        try (FileInputStream stream = new FileInputStream(path)) {
            String source = new String(stream.readAllBytes(), Charset.defaultCharset());
            run(source);
        }

        if (hadError) {
            System.exit(66);
        }
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

        for (var token : tokens) {
            System.out.println(token);
        }
    }

    static void error(int lineNo, String message) {
        report(lineNo, "", message);
    }

    private static void report(int lineNo, String where, String message) {
        System.err.println("[line " + lineNo + "] Error" + where + ": " + message);
        hadError = true;
    }
}

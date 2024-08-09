package tools;

import java.io.*;
import java.util.*;

public class GenerateAST {
    static final String INDENT = "    ";
    static final List<String> indents = new ArrayList<>();
    static final String EXPRESSION_NAME = "Expr";
    static final List<String> EXPRESSION_CLASSES = List.of(
            "Binary     : Expr left, Token operator, Expr right",
            "Unary      : Token operator, Expr operand",
            "Grouping   : Expr expression",
            "Literal    : Object value"
    );
    static final String STATEMENT_NAME = "Stmt";
    static final List<String> STATEMENT_CLASSES = List.of(
            "Expression : Expr expression",
            "Print      : Expr expression"
    );

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: GenerateAST dest");
            System.exit(69);
        }
        generate(EXPRESSION_NAME, EXPRESSION_CLASSES, args[0]);
        generate(STATEMENT_NAME, STATEMENT_CLASSES, args[0]);
    }

    private static void generate(String baseName, List<String> subclasses, String path) {
        try (PrintWriter writer = new PrintWriter(path + baseName + ".java")) {
            generateWithWriter(baseName, subclasses, writer);
        } catch (FileNotFoundException e) {
            System.err.println("File " + path + " not found: " + e.getLocalizedMessage());
            System.exit(68);
        } catch (IOException e) {
            System.err.println("Failure generating " + baseName + " classes: " + e.getLocalizedMessage());
            System.exit(67);
        }
    }

    private static void generateWithWriter(String baseName, List<String> subclasses, PrintWriter writer) throws IOException {
        writer.println("package jlox;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
        writer.println(
                indentBy(1) + "abstract public <T> T accept(" + baseName + "Visitor<T> visitor);");
        for (var subclass : subclasses) {
            generateSubclass(GeneratedClass.make(subclass, baseName), writer);
        }
        writer.println("}");
    }

    private static void generateSubclass(GeneratedClass classDescription, PrintWriter writer) {
        writer.println();
        writer.println(indentBy(1) + "static class " + classDescription.name + " extends " + classDescription.baseClass + " {");
        for (var field : classDescription.fields) {
            writer.println(indentBy(2) + "final " + field + ';');
        }

        writer.println();
        writer.println(indentBy(2) + "public " + classDescription.name + "("
                + classDescription.fields.stream()
                .map(field -> field.type + " " + field.name)
                .reduce((left, right) -> left + ", " + right)
                .orElse("")
                + ") {");
        for (var field : classDescription.fields) {
            writer.println(indentBy(3) + "this." + field.name + " = " + field.name + ';');
        }
        writer.println(indentBy(2) + '}');

        writer.println();
        writer.println(indentBy(2) + "@Override");
        writer.println(indentBy(2) + "public <T> T accept(" + classDescription.baseClass + "Visitor<T> visitor) {");
        writer.println(indentBy(3) + "return visitor.visit(this);");
        writer.println(indentBy(2) + '}');
        writer.println(indentBy(1) + '}');
    }

    private static String indentBy(int amount) {
        for (int i = indents.size(); i <= amount; i++) {
            indents.add(INDENT.repeat(i));
        }
        return indents.get(amount);
    }
}

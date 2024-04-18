package tools;

import java.io.*;
import java.util.*;

public class GenerateAST {
    static final String INDENT = "    ";
    static final String EXPRESSION_NAME = "Expr";
    static final List<String> EXPRESSION_CLASSES = List.of(
            "Binary     : Expr left, Token operator, Expr right",
            "Unary      : Token operator, Expr operand",
            "Grouping   : Expr expression",
            "Literal    : Object value"
    );
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: GenerateAST dest");
            System.exit(69);
        }
        try (PrintWriter writer = new PrintWriter(args[0] + "Expr.java")) {
            generate(EXPRESSION_NAME, EXPRESSION_CLASSES, writer);
        } catch (FileNotFoundException e) {
            System.err.println("File " + args[0] + " not found: " + e.getLocalizedMessage());
            System.exit(68);
        } catch (IOException e) {
            System.err.println("Failure generating expression classes: " + e.getLocalizedMessage());
            System.exit(67);
        }
    }

    private static void generate(String baseName, List<String> subclasses, PrintWriter writer) throws IOException {
        writer.println("package jlox;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
        for (var subclass : subclasses) {
            var split = subclass.split(":");
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
        writer.println(indentBy(2) + "public <T> T accept(ExprVisitor<T> visitor) {");
        writer.println(indentBy(3) + "return visitor.visit(this);");
        writer.println(indentBy(2) + '}');
        writer.println(indentBy(1) + '}');
    }

    private static String indentBy(int amount) {
        return INDENT.repeat(amount);
    }
}

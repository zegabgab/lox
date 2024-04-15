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
            generateSubclass(baseName, split[0].strip(), split[1].strip(), writer);
        }
        writer.println("}");
    }

    private static void generateSubclass(String baseClass, String subclass, String fields, PrintWriter writer) {
        writer.println(INDENT + "static class " + subclass + " extends " + baseClass + " {");
        var split = fields.split(", ");
        for (var field : split) {
            writer.println(INDENT + INDENT + "final " + field + ';');
        }
        writer.println();
        writer.println(INDENT + INDENT + "public " + subclass + "(" + fields + ") {");
        for (var field : split) {
            var fieldName = field.split(" ")[1];
            writer.println(INDENT + INDENT + INDENT + "this." + fieldName + " = " + fieldName + ';');
        }
        writer.println(INDENT + INDENT + '}');
        writer.println(INDENT + '}');
        writer.println();
    }
}

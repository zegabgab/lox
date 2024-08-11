package tools;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class GenerateAST {
    static final String INDENT = "    ";
    static final List<String> indents = new ArrayList<>();
    static final String EXPRESSION_NAME = "Expr";
    static final List<String> EXPRESSION_CLASSES = List.of(
            "Assign     : Token name, Expr value",
            "Logical    : Expr left, Token operator, Expr right",
            "Binary     : Expr left, Token operator, Expr right",
            "Unary      : Token operator, Expr operand",
            "Call       : Expr callee, Token parens, java.util.List<Expr> arguments",
            "Grouping   : Expr expression",
            "Literal    : Object value",
            "Variable   : Token name"
    );
    static final String STATEMENT_NAME = "Stmt";
    static final List<String> STATEMENT_CLASSES = List.of(
            "Block      : java.util.List<Stmt> statements",
            "Expression : Expr expression",
            "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
            "While      : Expr condition, Stmt body",
            "Print      : Expr expression",
            "Return     : Token keyword, Expr value",
            "Var        : Token name, Expr initializer",
            "Function   : Token name, java.util.List<Token> parameters, java.util.List<Stmt> body"
    );

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: GenerateAST dest");
            System.exit(69);
        }
        var expressionClasses = EXPRESSION_CLASSES.stream()
                .map(description -> GeneratedClass.make(description, EXPRESSION_NAME))
                .collect(Collectors.toList());
        var statementClasses = STATEMENT_CLASSES.stream()
                .map(description -> GeneratedClass.make(description, STATEMENT_NAME))
                .collect(Collectors.toList());

        generate(EXPRESSION_NAME, expressionClasses, args[0]);
        generateVisitor(EXPRESSION_NAME, expressionClasses, args[0]);
        generate(STATEMENT_NAME, statementClasses, args[0]);
        generateVisitor(STATEMENT_NAME, statementClasses, args[0]);
    }

    private static void generateVisitor(String baseName, List<GeneratedClass> subclasses, String path) {
        String fileName = path + baseName + "Visitor.java";
        try (PrintWriter writer = new PrintWriter(fileName)) {
            generateVisitorWithWriter(baseName, subclasses, writer);
        } catch (FileNotFoundException e) {
            System.err.println("File " + fileName + " not found: " + e.getLocalizedMessage());
            System.exit(68);
        }
    }

    private static void generateVisitorWithWriter(String baseName, List<GeneratedClass> subclasses, PrintWriter writer) {
        writer.println("package jlox;");
        writer.println();
        writer.println("interface " + baseName + "Visitor<T> {");
        for (var subclass : subclasses) {
            writer.println(indentBy(1) + "T visit(" + baseName + "." + subclass.name + " " + baseName.toLowerCase() + ");");
        }
        writer.println("}");
    }

    private static void generate(String baseName, List<GeneratedClass> subclasses, String path) {
        String fileName = path + baseName + ".java";
        try (PrintWriter writer = new PrintWriter(fileName)) {
            generateWithWriter(baseName, subclasses, writer);
        } catch (FileNotFoundException e) {
            System.err.println("File " + fileName + " not found: " + e.getLocalizedMessage());
            System.exit(68);
        }
    }

    private static void generateWithWriter(String baseName, List<GeneratedClass> subclasses, PrintWriter writer) {
        writer.println("package jlox;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
        writer.println(
                indentBy(1) + "abstract public <T> T accept(" + baseName + "Visitor<T> visitor);");
        for (var subclass : subclasses) {
            generateSubclass(subclass, writer);
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

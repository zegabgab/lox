package jlox;

import java.util.*;

class Resolver implements ExprVisitor<Void>, StmtVisitor<Void> {
    private final Interpreter interpreter;
    private final Stack<HashMap<String, Integer>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE,
        FUNCTION,
        METHOD,
        INITIALIZER,
    }

    private enum ClassType {
        NONE,
        CLASS,
    }

    public void resolve(List<Stmt> statements) {
        for (var statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt statement) {
        statement.accept(this);
    }

    private void resolve(Expr expression) {
        expression.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    @Override
    public Void visit(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visit(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visit(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visit(Expr.Unary expr) {
        resolve(expr.operand);
        return null;
    }

    @Override
    public Void visit(Expr.Call expr) {
        resolve(expr.callee);
        for (var argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visit(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visit(Expr.Set expr) {
        resolve(expr.object);
        resolve(expr.value);
        return null;
    }

    @Override
    public Void visit(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visit(Expr.This expr) {
        if (currentClass.equals(ClassType.NONE)) {
            Lox.error(expr.keyword, "Can't use 'this' outside of a class");
        } else {
            resolveLocal(expr, expr.keyword);
        }

        return null;
    }

    @Override
    public Void visit(Expr.Literal expr) {
        return null;
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i, scopes.get(i).get(name.lexeme));
                return;
            }
        }
    }

    @Override
    public Void visit(Expr.Variable expr) {
        if (!scopes.isEmpty()
                && scopes.peek().containsKey(expr.name.lexeme)
                && scopes.peek().get(expr.name.lexeme) < 0) {
            Lox.error(expr.name, "Can't read local variable in its own initializer");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visit(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visit(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visit(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visit(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visit(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visit(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code");
        } else if (currentFunction == FunctionType.INITIALIZER && stmt.value != null) {
            Lox.error(stmt.keyword, "Can't return a value from an initializer");
        }
        if (stmt.value != null) {
            resolve(stmt.value);
        }
        return null;
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) {
            return;
        }

        var scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Variable " + name.lexeme + " already declared in this scope");
        }
        scope.put(name.lexeme, -1);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }

        scopes.peek().put(name.lexeme, scopes.peek().size() - 1);
    }

    @Override
    public Void visit(Stmt.Var stmt) {
        declare(stmt.name);
        resolve(stmt.initializer);
        define(stmt.name);
        return null;
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        var enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (var parameter : function.parameters) {
            declare(parameter);
            define(parameter);
        }

        resolve(function.body);
        endScope();

        currentFunction = enclosingFunction;
    }

    @Override
    public Void visit(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visit(Stmt.Class stmt) {
        var enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);
        beginScope();
        scopes.peek().put("this", scopes.peek().size());

        for (var function : stmt.methods) {
            var declaration = function.name.lexeme.equals("init")
                    ? FunctionType.INITIALIZER
                    : FunctionType.METHOD;
            resolveFunction(function, declaration);
        }

        endScope();
        currentClass = enclosingClass;

        return null;
    }
}

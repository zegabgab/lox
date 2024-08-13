package jlox;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {
    private final GlobalEnvironment globals = new GlobalEnvironment();
    private Environment environment = null;
    private final HashMap<Expr, VarCoordinates> locals = new HashMap<>();

    private static class VarCoordinates {
        final int distance;
        final int index;

        private VarCoordinates(int distance, int index) {
            this.distance = distance;
            this.index = index;
        }
    }

    public Interpreter() {
        globals.define("clock", new Clock());
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (var statement : statements) {
                statement.accept(this);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }
        String string = object.toString();
        if (object instanceof Double && string.endsWith(".0")) {
            return string.substring(0, string.length() - 2);
        }
        return string;
    }

    private boolean isTruthy(Object object) {
        return object != null && !object.equals(false);
    }

    @Override
    public Object visit(Expr.Unary expr) {
        if (expr.operator.type.equals(TokenType.BANG)) {
            var operand = expr.operand.accept(this);
            return !isTruthy(operand);
        } else if (expr.operator.type.equals(TokenType.MINUS)) {
            var operand = expr.operand.accept(this);
            if (operand instanceof Double) {
                return -(Double) operand;
            }
        }

        return null;
    }

    @Override
    public Object visit(Expr.Call expr) {
        var callee = expr.callee.accept(this);
        List<Object> arguments = expr.arguments.stream()
                .map(argument -> argument.accept(this)).collect(Collectors.toList());

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.parens, "Can only call functions and classes");
        }

        var function = (LoxCallable) callee;
        if (function.arity() != arguments.size()) {
            throw new RuntimeError(expr.parens, "Expected " +
                    function.arity() +
                    " arguments but got " +
                    arguments.size());
        }

        return function.call(this, arguments);
    }

    private Boolean compare(Expr.Binary binary, BiPredicate<Double, Double> comparator) {
        var left = binary.left.accept(this);
        var right = binary.right.accept(this);

        if (left instanceof Double && right instanceof Double) {
            return comparator.test((Double) left, (Double) right);
        }

        throw new RuntimeError(binary.operator, "Comparison requires two numbers as operands");
    }

    private Object add(Expr.Binary binary) {
        var left = binary.left.accept(this);
        var right = binary.right.accept(this);

        if (left instanceof String || right instanceof String) {
            return stringify(left) + stringify(right);
        }
        if (left instanceof Double && right instanceof Double) {
            return (Double) left + (Double) right;
        }

        throw new RuntimeError(binary.operator, "Addition requires two numbers or at least one string as operands");
    }

    private Object subtract(Expr.Binary binary) {
        var left = binary.left.accept(this);
        var right = binary.right.accept(this);

        if (left instanceof Double && right instanceof Double) {
            return (Double) left - (Double) right;
        }

        throw new RuntimeError(binary.operator, "Subtraction requires two numbers as operands");
    }

    private Object multiply(Expr.Binary binary) {
        var left = binary.left.accept(this);
        var right = binary.right.accept(this);

        if (left instanceof Double && right instanceof Double) {
            return (Double) left * (Double) right;
        }

        throw new RuntimeError(binary.operator, "Multiplication requires two numbers as operands");
    }

    private Object divide(Expr.Binary binary) {
        var left = binary.left.accept(this);
        var right = binary.right.accept(this);

        if (left instanceof Double && right instanceof Double) {
            if (right.equals(0.)) {
                throw new RuntimeError(binary.operator, "Division by zero");
            }

            return (Double) left / (Double) right;
        }

        throw new RuntimeError(binary.operator, "Division requires two numbers as operands");
    }

    @Override
    public Object visit(Expr.Assign expr) {
        var value = expr.value.accept(this);
        var coordinates = locals.get(expr);

        return coordinates != null ? environment.assignAt(coordinates.distance, coordinates.index, value)
                : globals.assign(expr.name, value);
    }

    @Override
    public Object visit(Expr.Logical expr) {
        var left = expr.left.accept(this);
        if (expr.operator.type.equals(TokenType.AND)) {
            return isTruthy(left) ? expr.right.accept(this) : left;
        }

        return isTruthy(left) ? left : expr.right.accept(this);
    }

    @Override
    public Object visit(Expr.Binary expr) {
        TokenType operator = expr.operator.type;
        switch (operator) {
            case EQUAL_EQUAL:
                return Objects.equals(expr.left.accept(this), expr.right.accept(this));
            case BANG_EQUAL:
                return !Objects.equals(expr.left.accept(this), expr.right.accept(this));
            case LESS:
                return compare(expr, (left, right) -> left < right);
            case LESS_EQUAL:
                return compare(expr, (left, right) -> left <= right);
            case GREATER:
                return compare(expr, (left, right) -> left > right);
            case GREATER_EQUAL:
                return compare(expr, (left, right) -> left >= right);
            case PLUS:
                return add(expr);
            case MINUS:
                return subtract(expr);
            case STAR:
                return multiply(expr);
            case SLASH:
                return divide(expr);
        }

        throw new RuntimeError(expr.operator, "Unexpected token");
    }

    @Override
    public Object visit(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public Object visit(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visit(Expr.Variable expr) {
        var coordinates = locals.get(expr);
        return coordinates != null ? environment.getAt(coordinates.distance, coordinates.index)
                : globals.get(expr.name);
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (var statement : statements) {
                statement.accept(this);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visit(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visit(Stmt.Expression stmt) {
        stmt.expression.accept(this);
        return null;
    }

    @Override
    public Void visit(Stmt.If stmt) {
        if (isTruthy(stmt.condition.accept(this))) {
            stmt.thenBranch.accept(this);
        } else {
            stmt.elseBranch.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Stmt.While stmt) {
        while (isTruthy(stmt.condition.accept(this))) {
            stmt.body.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Stmt.Print stmt) {
        System.out.println(stringify(stmt.expression.accept(this)));
        return null;
    }

    @Override
    public Void visit(Stmt.Return stmt) {
        throw new Return(stmt.value.accept(this));
    }

    @Override
    public Void visit(Stmt.Var stmt) {
        var value = stmt.initializer.accept(this);
        if (environment != null) {
            environment.define(value);
        } else {
            globals.define(stmt.name.lexeme, value);
        }
        return null;
    }

    @Override
    public Void visit(Stmt.Function stmt) {
        var function = new LoxFunction(stmt, environment);
        if (environment != null) {
            environment.define(function);
        } else {
            globals.define(stmt.name.lexeme, function);
        }
        return null;
    }

    public void resolve(Expr expr, int depth, int index) {
        locals.put(expr, new VarCoordinates(depth, index));
    }
}

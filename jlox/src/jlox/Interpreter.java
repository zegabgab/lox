package jlox;

import java.util.*;
import java.util.function.*;

class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {
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
        return object != null && !object.equals(Boolean.FALSE);
    }

    @Override
    public Object visit(Expr.Unary unary) {
        if (unary.operator.type.equals(TokenType.BANG)) {
            var operand = unary.operand.accept(this);
            return !isTruthy(operand);
        } else if (unary.operator.type.equals(TokenType.MINUS)) {
            var operand = unary.operand.accept(this);
            if (operand instanceof Double) {
                return -(Double) operand;
            }
        }

        return null;
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
    public Object visit(Expr.Binary binary) {
        TokenType operator = binary.operator.type;
        switch (operator) {
            case EQUAL_EQUAL:
                return Objects.equals(binary.left.accept(this), binary.right.accept(this));
            case BANG_EQUAL:
                return !Objects.equals(binary.left.accept(this), binary.right.accept(this));
            case LESS:
                return compare(binary, (left, right) -> left < right);
            case LESS_EQUAL:
                return compare(binary, (left, right) -> left <= right);
            case GREATER:
                return compare(binary, (left, right) -> left > right);
            case GREATER_EQUAL:
                return compare(binary, (left, right) -> left >= right);
            case PLUS:
                return add(binary);
            case MINUS:
                return subtract(binary);
            case STAR:
                return multiply(binary);
            case SLASH:
                return divide(binary);
        }

        throw new RuntimeError(binary.operator, "Unexpected token");
    }

    @Override
    public Object visit(Expr.Grouping grouping) {
        return grouping.expression.accept(this);
    }

    @Override
    public Object visit(Expr.Literal literal) {
        return literal.value;
    }

    @Override
    public Void visit(Stmt.Expression expression) {
        expression.expression.accept(this);
        return null;
    }

    @Override
    public Void visit(Stmt.Print print) {
        System.out.println(stringify(print.expression.accept(this)));
        return null;
    }
}

package jlox;

import java.util.*;

import static jlox.TokenType.*;

class Parser {private final List<Token> tokens;
    int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token current() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return current().type.equals(type);
    }

    private boolean match(TokenType... types) {
        if (Arrays.stream(types).anyMatch(this::check)) {
            current++;
            return true;
        }
        return false;
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            return new Expr.Unary(previous(), unary());
        }
        return primary();
    }

    private Expr primary() {
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            if (match(RIGHT_PAREN)) {
                return new Expr.Grouping(expr);
            }
            Lox.error(previous().lineNo, "Expected ')'");
            return null;
        }
        if (match(TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(NIL)) {
            return new Expr.Literal(null);
        }
        if (match(NUMBER)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(STRING)) {
            return new Expr.Literal(previous().literal);
        }
        Lox.error(previous().lineNo, "Expected literal");
        return null;
    }
}

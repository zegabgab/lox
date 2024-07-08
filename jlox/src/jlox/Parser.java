package jlox;

import java.util.*;

import static jlox.TokenType.*;

class Parser {
    private final List<Token> tokens;
    int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Optional<Expr> parse() {
        try {
            return Optional.of(expression());
        } catch (ParseError ignored) {
            return Optional.empty();
        }
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

    private void advance() {
        if (!isAtEnd()) {
            current++;
        }
    }

    private boolean match(TokenType... types) {
        if (Arrays.stream(types).anyMatch(this::check)) {
            advance();
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

    private static class ParseError extends RuntimeException {}

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type.equals(SEMICOLON)) {
                return;
            }

            switch (current().type) {
                case CLASS:
                case FUN:
                case VAR:
                case IF:
                case WHILE:
                case FOR:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

    private Token consume(TokenType type, String errorMessage) {
        if (match(type)) {
            return previous();
        }

        throw error(current(), errorMessage);
    }

    private Expr primary() {
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
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')'");
            return new Expr.Grouping(expr);
        }
        throw error(current(), "Expected expression");
    }
}

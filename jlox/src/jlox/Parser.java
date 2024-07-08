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
        } catch (ParseException except) {
            Lox.error(except.getToken(), except.getMessage());
            return Optional.empty();
        }
    }

    private Token peek() {
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
        return peek().type.equals(type);
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

    private Expr expression() throws ParseException {
        return equality();
    }

    private Expr equality() throws ParseException {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() throws ParseException {
        Expr expr = term();
        while (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() throws ParseException {
        Expr expr = factor();
        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() throws ParseException {
        Expr expr = unary();
        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() throws ParseException {
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

            switch (peek().type) {
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

        throw error(peek(), errorMessage);
    }

    private Expr primary() throws ParseException {
        ParseResult result;
        if (match(TRUE)) {
            result = new ParseSuccess(new Expr.Literal(true), current);
        } else if (match(FALSE)) {
            result = new ParseSuccess(new Expr.Literal(false), current);
        } else if (match(NIL)) {
            result = new ParseSuccess(new Expr.Literal(null), current);
        } else if (match(NUMBER)) {
            result = new ParseSuccess(new Expr.Literal(previous().literal), current);
        } else if (match(STRING)) {
            result = new ParseSuccess(new Expr.Literal(previous().literal), current);
        } else if (match(LEFT_PAREN)) {
            Expr expr = expression();
            result = check(RIGHT_PAREN)
                    ? new ParseSuccess(new Expr.Grouping(expr), current + 1)
                    : new ParseFailure(peek(), "Expected ')'");
        } else {
            result = new ParseFailure(peek(), "Expected expression");
        }
        return result.unwrap();
    }
}

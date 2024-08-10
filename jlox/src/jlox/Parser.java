package jlox;

import java.util.*;

import static jlox.TokenType.*;

class Parser {
    private final List<Token> tokens;
    int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        ArrayList<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            var declaration = declaration();
            if (declaration != null) {
                statements.add(declaration);
            }
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            return match(VAR) ? varDeclaration() : statement();
        } catch (ParseException e) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() throws  ParseException {
        var token = consume(IDENTIFIER, "Expected identifier");
        if (match(SEMICOLON)) {
            return new Stmt.Var(token, new Expr.Literal(null));
        }
        consume(EQUAL, "Expected '='");
        var expression = expression();
        consume(SEMICOLON, "Expected semicolon");
        return new Stmt.Var(token, expression);
    }

    private Stmt statement() throws ParseException {
        if (match(PRINT)) {
            var expr = expression();
            consume(SEMICOLON, "Expected semicolon");
            return new Stmt.Print(expr);
        }

        var expr = expression();
        consume(SEMICOLON, "Expected semicolon");
        return new Stmt.Expression(expr);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return current >= tokens.size() || peek().type.equals(EOF);
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

    private ParseException error(Token token, String message) {
        Lox.error(token, message);
        return new ParseException(token, message);
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

    private Token consume(TokenType type, String errorMessage) throws ParseException {
        if (match(type)) {
            return previous();
        }

        throw error(peek(), errorMessage);
    }


    private Expr primary() throws ParseException {
        if (match(NIL)) {
            return new Expr.Literal(null);
        }
        if (match(TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(LEFT_PAREN)) {
            var expr = expression();
            consume(RIGHT_PAREN, "Expected closing bracket");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expected expression");
    }
}

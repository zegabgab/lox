package jlox;

import java.util.*;

import static jlox.TokenType.*;

class Parser {
    private static final Stmt EMPTY_STATEMENT = new Stmt.Expression(new Expr.Literal(null));
    private static final Expr NIL_EXPRESSION = new Expr.Literal(null);
    private static final Expr TRUE_EXPRESSION = new Expr.Literal(true);
    private static final Expr FALSE_EXPRESSION = new Expr.Literal(false);

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
        current = 0;
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
            return new Stmt.Var(token, NIL_EXPRESSION);
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
        } else if (match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        } else if (match(IF)) {
            return ifStatement();
        } else if (match(WHILE)) {
            return whileStatement();
        } else if (match(FOR)) {
            return forStatement();
        }

        return expressionStatement();
    }

    private Stmt expressionStatement() throws ParseException {
        var expr = expression();
        consume(SEMICOLON, "Expected semicolon");
        return new Stmt.Expression(expr);
    }

    private Stmt forStatement() throws ParseException {
        consume(LEFT_PAREN, "Expected '(' after 'for'");
        var initializer = match(SEMICOLON) ? EMPTY_STATEMENT
                : match(VAR) ? varDeclaration()
                : expressionStatement();
        var condition = check(SEMICOLON) ? TRUE_EXPRESSION : expression();
        consume(SEMICOLON, "Expected semicolon");
        var increment = check(RIGHT_PAREN) ? NIL_EXPRESSION : expression();
        consume(RIGHT_PAREN, "Expected ')' after for header");
        var body = statement();
        return new Stmt.Block(List.of(
                initializer,
                new Stmt.While(condition, new Stmt.Block(List.of(
                        body,
                        new Stmt.Expression(increment)
                )))
        ));
    }

    private Stmt whileStatement() throws ParseException {
        consume(LEFT_PAREN, "Expected '(' after 'while'");
        var condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after while condition");
        var body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() throws ParseException {
        consume(LEFT_PAREN, "Expected '(' after 'if'");
        var condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition");
        var thenStatement = statement();
        if (match(ELSE)) {
            var elseStatement = statement();
            return new Stmt.If(condition, thenStatement, elseStatement);
        }
        return new Stmt.If(condition, thenStatement, EMPTY_STATEMENT);
    }

    private ArrayList<Stmt> block() throws ParseException {
        ArrayList<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expected '}' after block");
        return statements;
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
        return assignment();
    }

    private Expr assignment() throws ParseException {
        var expr = or();
        if (match(EQUAL)) {
            Token equals = previous();
            var value = assignment();
            if (expr instanceof Expr.Variable) {
                return new Expr.Assign(((Expr.Variable) expr).name, value);
            }

            error(equals, "Invalid assignment target");
        }

        return expr;
    }

    private Expr or() throws ParseException {
        var expr = and();
        while (match(OR)) {
            var operator = previous();
            var right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() throws ParseException {
        var expr = equality();
        while (match(AND)) {
            var operator = previous();
            var right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
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
            return NIL_EXPRESSION;
        }
        if (match(TRUE)) {
            return TRUE_EXPRESSION;
        }
        if (match(FALSE)) {
            return FALSE_EXPRESSION;
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

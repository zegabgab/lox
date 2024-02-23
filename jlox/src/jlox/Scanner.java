package jlox;

import java.util.*;

class Scanner {
    private final String source;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    TokenType.AND);
        keywords.put("class",  TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        List<Token> tokens = new ArrayList<>();
        while (!isAtEnd()) {
            start = current;
            addToken(tokens);
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void addToken(List<Token> tokens) {
        Token token = scanToken();
        if (token != null) {
            tokens.add(token);
        }
    }

    private Token scanToken() {
        char c = advance();
        switch (c) {
            case '(': return token(TokenType.LEFT_PAREN);
            case ')': return token(TokenType.RIGHT_PAREN);
            case '{': return token(TokenType.LEFT_BRACE);
            case '}': return token(TokenType.RIGHT_BRACE);
            case ',': return token(TokenType.COMMA);
            case '.': return token(TokenType.DOT);
            case '-': return token(TokenType.MINUS);
            case '+': return token(TokenType.PLUS);
            case ';': return token(TokenType.SEMICOLON);
            case '*': return token(TokenType.STAR);
            case '!':
                return token(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
            case '=':
                return token(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '<':
                return token(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>':
                return token(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    return token(TokenType.SLASH);
                }
                break;
            case ' ':
            case '\t':
            case '\r':
                break;
            case '\n':
                line++;
                break;
            case '"': return string();
            default:
                if (isDigit(c)) {
                    return number();
                } else if (isLetter(c)) {
                    return identifier();
                }
                Lox.error(line, "Unexpected character: " + c);
        }

        return null;
    }

    private Token identifier() {
        while (isAlphaNumeric(peek())) advance();
        var identifier = source.substring(start, current);
        return keywords.containsKey(identifier) ?
                token(keywords.get(identifier)) :
                token(TokenType.IDENTIFIER, identifier);
    }

    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isLetter(c);
    }

    private boolean isLetter(char c) {
        return (c <= 'z' && c >= 'a') ||
                (c <= 'Z' && c >= 'A') ||
                (c == '_');
    }

    private Token number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }
        var number = Double.valueOf(source.substring(start, current));
        return token(TokenType.NUMBER, number);
    }

    private char peekNext() {
        return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private Token string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string");
            return null;
        }

        advance();

        return token(TokenType.STRING, source.substring(start + 1, current - 1));
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private boolean match(char expected) {
        if (peek() != expected) return false;
        advance();
        return true;
    }

    private Token token(TokenType type) {
        return new Token(type, source.substring(start, current), line);
    }

    private Token token(TokenType type, Object literal) {
        return new Token(type, source.substring(start, current), literal, line);
    }

    private char advance() {
        if (isAtEnd()) return '\0';
        return source.charAt(current++);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}

package jlox;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int lineNo;

    public Token(TokenType type, String lexeme, Object literal, int lineNo) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.lineNo = lineNo;
    }

    public Token(TokenType type, String lexeme, int lineNo) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = null;
        this.lineNo = lineNo;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + (literal != null ? literal.toString() : "");
    }
}

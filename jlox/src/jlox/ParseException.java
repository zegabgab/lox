package jlox;

class ParseException extends Exception {
    private final Token token;
    public ParseException(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}

package jlox;

public class RuntimeError extends RuntimeException {
    public final Token cause;

    public RuntimeError(Token cause, String message) {
        super(message);
        this.cause = cause;
    }
}

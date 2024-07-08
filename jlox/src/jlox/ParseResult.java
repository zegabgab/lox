package jlox;

import java.util.function.*;

interface ParseResult {
    ParseResult then(Function<? super ParseSuccess, ? extends ParseResult> next);
    ParseResult or(Function<? super ParseFailure, ? extends ParseResult> other);
    Expr unwrap() throws ParseException;
}

final class ParseFailure implements ParseResult {
    @Override
    public ParseResult or(Function<? super ParseFailure, ? extends ParseResult> other) {
        return other.apply(this);
    }

    private final Token token;
    private final String message;

    ParseFailure(Token token, String message) {
        this.token = token;
        this.message = message;
    }

    @Override
    public ParseResult then(Function<? super ParseSuccess, ? extends ParseResult> mapper) {
        return this;
    }

    @Override
    public Expr unwrap() throws ParseException {
        throw new ParseException(token, message);
    }
}

final class ParseSuccess implements ParseResult {
    private final Expr result;
    private final int end;

    ParseSuccess(Expr result, int end) {
        this.result = result;
        this.end = end;
    }

    public Expr getResult() {
        return result;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public ParseResult then(Function<? super ParseSuccess, ? extends ParseResult> mapper) {
        return mapper.apply(this);
    }

    @Override
    public ParseResult or(Function<? super ParseFailure, ? extends ParseResult> other) {
        return this;
    }

    @Override
    public Expr unwrap() {
        return result;
    }
}
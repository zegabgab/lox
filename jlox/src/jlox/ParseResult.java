package jlox;

import java.util.function.BiFunction;

interface ParseResult {
    ParseResult map(BiFunction<Integer, ? super Expr, ? extends ParseResult> mapper);
    Expr unwrap(String message) throws ParseException;
}

final class ParseFailure implements ParseResult {
    @Override
    public ParseResult map(BiFunction<Integer, ? super Expr, ? extends ParseResult> mapper) {
        return this;
    }

    @Override
    public Expr unwrap(String message) throws ParseException {
        throw new ParseException(message);
    }
}

final class ParseSuccess implements ParseResult {
    private final Expr result;
    private final int end;

    ParseSuccess(Expr result, int end) {
        this.result = result;
        this.end = end;
    }

    @Override
    public ParseResult map(BiFunction<Integer, ? super Expr, ? extends ParseResult> mapper) {
        return mapper.apply(end, result);
    }

    @Override
    public Expr unwrap(String message) {
        return result;
    }
}
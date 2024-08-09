package jlox;

interface ExprVisitor<T> {
    T visit(Expr.Binary binary);
    T visit(Expr.Unary unary);
    T visit(Expr.Grouping grouping);
    T visit(Expr.Literal literal);
}

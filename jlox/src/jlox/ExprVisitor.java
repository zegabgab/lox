package jlox;

interface ExprVisitor<T> {
    T visit(Expr expression);
    T visit(Expr.Unary unary);
    T visit(Expr.Binary binary);
    T visit(Expr.Grouping grouping);
}

package jlox;

interface StmtVisitor<T> {
    T visit(Stmt statement);
    T visit(Stmt.Print print);
    T visit(Stmt.Expression expression);
}

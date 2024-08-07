package jlox;

interface StmtVisitor<T> {
    T visit(Stmt.Expression expression);
    T visit(Stmt.Print print);
}

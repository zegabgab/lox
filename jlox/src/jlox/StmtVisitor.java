package jlox;

interface StmtVisitor<T> {
    T visit(Stmt.Block block);
    T visit(Stmt.Expression expression);
    T visit(Stmt.Print print);
    T visit(Stmt.Var var);
}

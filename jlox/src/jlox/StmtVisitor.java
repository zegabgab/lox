package jlox;

interface StmtVisitor<T> {
    T visit(Stmt.Block stmt);
    T visit(Stmt.Expression stmt);
    T visit(Stmt.If stmt);
    T visit(Stmt.While stmt);
    T visit(Stmt.Print stmt);
    T visit(Stmt.Var stmt);
}

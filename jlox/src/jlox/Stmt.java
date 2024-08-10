package jlox;

abstract class Stmt {
    abstract public <T> T accept(StmtVisitor<T> visitor);

    static class Expression extends Stmt {
        final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Print extends Stmt {
        final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Var extends Stmt {
        final Token name;
        final Expr initializer;

        public Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}

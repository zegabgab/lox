package jlox;

abstract class Stmt {
    abstract public <T> T accept(StmtVisitor<T> visitor);

    static class Block extends Stmt {
        final java.util.List<Stmt> statements;

        public Block(java.util.List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

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

    static class If extends Stmt {
        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class While extends Stmt {
        final Expr condition;
        final Stmt body;

        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
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

    static class Return extends Stmt {
        final Token keyword;
        final Expr value;

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
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

    static class Function extends Stmt {
        final Token name;
        final java.util.List<Token> parameters;
        final java.util.List<Stmt> body;

        public Function(Token name, java.util.List<Token> parameters, java.util.List<Stmt> body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Class extends Stmt {
        final Token name;
        final Expr.Variable superclass;
        final java.util.List<Stmt.Function> methods;

        public Class(Token name, Expr.Variable superclass, java.util.List<Stmt.Function> methods) {
            this.name = name;
            this.superclass = superclass;
            this.methods = methods;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}

package jlox;

abstract class Expr {
    abstract public <T> T accept(ExprVisitor<T> visitor);

    static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Unary extends Expr {
        final Token operator;
        final Expr operand;

        public Unary(Token operator, Expr operand) {
            this.operator = operator;
            this.operand = operand;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Grouping extends Expr {
        final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Literal extends Expr {
        final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Variable extends Expr {
        final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}

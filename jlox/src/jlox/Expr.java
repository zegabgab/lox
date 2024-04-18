package jlox;

abstract class Expr {

    static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

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

        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Grouping extends Expr {
        final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Literal extends Expr {
        final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}

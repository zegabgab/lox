package jlox;

abstract class Expr {
    abstract public <T> T accept(ExprVisitor<T> visitor);

    static class Assign extends Expr {
        final Token name;
        final Expr value;

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Logical extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

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

    static class Call extends Expr {
        final Expr callee;
        final Token parens;
        final java.util.List<Expr> arguments;

        public Call(Expr callee, Token parens, java.util.List<Expr> arguments) {
            this.callee = callee;
            this.parens = parens;
            this.arguments = arguments;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Get extends Expr {
        final Expr object;
        final Token name;

        public Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Set extends Expr {
        final Expr object;
        final Token name;
        final Expr value;

        public Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    static class Super extends Expr {
        final Token keyword;
        final Token method;

        public Super(Token keyword, Token method) {
            this.keyword = keyword;
            this.method = method;
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

    static class This extends Expr {
        final Token keyword;

        public This(Token keyword) {
            this.keyword = keyword;
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

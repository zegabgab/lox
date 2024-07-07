package jlox;

class ReversePolishTranslator implements ExprVisitor<String> {
    public String print(Expr expression) {
        return expression.accept(this);
    }

    @Override
    public String visit(Expr.Unary unary) {
        return unary.operand.accept(this) + " " + unary.operator.lexeme;
    }

    @Override
    public String visit(Expr.Binary binary) {
        return binary.left.accept(this) + " " + binary.right.accept(this) + " " + binary.operator.lexeme;
    }

    @Override
    public String visit(Expr.Grouping grouping) {
        return grouping.expression.accept(this) + " group";
    }

    @Override
    public String visit(Expr.Literal literal) {
        return literal.value != null ? literal.value.toString() : "nil";
    }
}

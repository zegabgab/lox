package jlox;

class PrettyPrinter implements ExprVisitor<String> {
    private static String parenthesize(String expression) {
        return "(" + expression + ")";
    }

    public String print(Expr expression) {
        return expression.accept(this);
    }

    @Override
    public String visit(Expr.Unary unary) {
        return parenthesize(unary.operator.lexeme + " " + unary.operand.accept(this));
    }

    @Override
    public String visit(Expr.Binary binary) {
        return parenthesize(binary.operator.lexeme + " " + binary.left.accept(this) + " " + binary.right.accept(this));
    }

    @Override
    public String visit(Expr.Grouping grouping) {
        return parenthesize("group " + grouping.expression.accept(this));
    }

    @Override
    public String visit(Expr.Literal literal) {
        return literal.value == null ? "nil" : literal.value.toString();
    }
}

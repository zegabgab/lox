package jlox;

class PrettyPrinter implements ExprVisitor<String> {
    private static String parenthesize(String expression) {
        return "(" + expression + ")";
    }

    public String print(Expr expression) {
        return expression.accept(this);
    }

    @Override
    public String visit(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme + " " + expr.operand.accept(this));
    }

    @Override
    public String visit(Expr.Assign expr) {
        return parenthesize("assign " + expr.name.lexeme + " " + expr.value.accept(this));
    }

    @Override
    public String visit(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme + " " + expr.left.accept(this) + " " + expr.right.accept(this));
    }

    @Override
    public String visit(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme + " " + expr.left.accept(this) + " " + expr.right.accept(this));
    }

    @Override
    public String visit(Expr.Grouping expr) {
        return parenthesize("group " + expr.expression.accept(this));
    }

    @Override
    public String visit(Expr.Literal expr) {
        return expr.value == null ? "nil" : expr.value.toString();
    }

    @Override
    public String visit(Expr.Variable expr) {
        return expr.name.lexeme;
    }
}

package org.example.sintatico;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    public String print(Stmt stmt) {
        return stmt.accept(this);
    }

    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitVar(Stmt.Var stmt) {
        String init = stmt.initializer != null ? (" = " + print(stmt.initializer)) : "";
        return "var(" + stmt.typeKeyword + " " + stmt.name + init + ")";
    }

    @Override
    public String visitExprStmt(Stmt.ExprStmt stmt) {
        return "expr(" + print(stmt.expression) + ")";
    }

    @Override
    public String visitPrint(Stmt.Print stmt) {
        return "print(" + print(stmt.expression) + ")";
    }

    @Override
    public String visitBlock(Stmt.Block stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("block{");
        boolean first = true;
        for (Stmt s : stmt.statements) {
            if (!first) sb.append(", ");
            sb.append(print(s));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitIf(Stmt.If stmt) {
        String elsePart = stmt.elseBranch != null ? (", else=" + print(stmt.elseBranch)) : "";
        return "if(cond=" + print(stmt.condition) + ", then=" + print(stmt.thenBranch) + elsePart + ")";
    }

    @Override
    public String visitWhile(Stmt.While stmt) {
        return "while(cond=" + print(stmt.condition) + ", body=" + print(stmt.body) + ")";
    }

    @Override
    public String visitLiteral(Expr.Literal expr) {
        return String.valueOf(expr.value);
    }

    @Override
    public String visitVariable(Expr.Variable expr) {
        return expr.name;
    }

    @Override
    public String visitGrouping(Expr.Grouping expr) {
        return "(" + print(expr.expression) + ")";
    }

    @Override
    public String visitUnary(Expr.Unary expr) {
        return "(" + expr.operator + " " + print(expr.right) + ")";
    }

    @Override
    public String visitBinary(Expr.Binary expr) {
        return "(" + print(expr.left) + " " + expr.operator + " " + print(expr.right) + ")";
    }

    @Override
    public String visitLogical(Expr.Logical expr) {
        return "(" + print(expr.left) + " " + expr.operator + " " + print(expr.right) + ")";
    }
}

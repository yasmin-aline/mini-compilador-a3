package org.example.sintatico;

public abstract class Expr {
    public interface Visitor<R> {
        R visitLiteral(Literal expr);
        R visitVariable(Variable expr);
        R visitGrouping(Grouping expr);
        R visitUnary(Unary expr);
        R visitBinary(Binary expr);
        R visitLogical(Logical expr);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Literal extends Expr {
        public final Object value;
        public Literal(Object value) { this.value = value; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitLiteral(this); }
    }

    public static class Variable extends Expr {
        public final String name;
        public Variable(String name) { this.name = name; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitVariable(this); }
    }

    public static class Grouping extends Expr {
        public final Expr expression;
        public Grouping(Expr expression) { this.expression = expression; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitGrouping(this); }
    }

    public static class Unary extends Expr {
        public final String operator;
        public final Expr right;
        public Unary(String operator, Expr right) { this.operator = operator; this.right = right; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitUnary(this); }
    }

    public static class Binary extends Expr {
        public final Expr left;
        public final String operator;
        public final Expr right;
        public Binary(Expr left, String operator, Expr right) { this.left = left; this.operator = operator; this.right = right; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitBinary(this); }
    }

    public static class Logical extends Expr {
        public final Expr left;
        public final String operator;
        public final Expr right;
        public Logical(Expr left, String operator, Expr right) { this.left = left; this.operator = operator; this.right = right; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitLogical(this); }
    }
}
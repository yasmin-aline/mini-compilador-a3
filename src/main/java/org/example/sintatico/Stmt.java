package org.example.sintatico;

import java.util.List;

public abstract class Stmt {
    public interface Visitor<R> {
        R visitVar(Var stmt);
        R visitExprStmt(ExprStmt stmt);
        R visitPrint(Print stmt);
        R visitRead(Read stmt);
        R visitBlock(Block stmt);
        R visitIf(If stmt);
        R visitWhile(While stmt);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Var extends Stmt {
        public final String typeKeyword;
        public final String name;
        public final Expr initializer;
        public Var(String typeKeyword, String name, Expr initializer) {
            this.typeKeyword = typeKeyword; this.name = name; this.initializer = initializer;
        }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitVar(this); }
    }

    public static class ExprStmt extends Stmt {
        public final Expr expression;
        public ExprStmt(Expr expression) { this.expression = expression; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitExprStmt(this); }
    }

    public static class Print extends Stmt {
        public final Expr expression;
        public Print(Expr expression) { this.expression = expression; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitPrint(this); }
    }

    public static class Read extends Stmt {
        public final String name;
        public Read(String name) { this.name = name; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitRead(this); }
    }

    public static class Block extends Stmt {
        public final List<Stmt> statements;
        public Block(List<Stmt> statements) { this.statements = statements; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitBlock(this); }
    }

    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition; this.thenBranch = thenBranch; this.elseBranch = elseBranch;
        }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitIf(this); }
    }

    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt body;
        public While(Expr condition, Stmt body) { this.condition = condition; this.body = body; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitWhile(this); }
    }
}

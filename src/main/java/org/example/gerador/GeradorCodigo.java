package org.example.gerador;

import org.example.sintatico.Expr;
import org.example.sintatico.Stmt;
import org.example.sintatico.Stmt.Read;

public class GeradorCodigo implements Stmt.Visitor<String>, Expr.Visitor<String> {

public String gerarClasseJava(java.util.List<Stmt> statements) {
        StringBuilder codigo = new StringBuilder();
        
        codigo.append("import java.util.Scanner;\n\n");
        codigo.append("public class ProgramaCompilado {\n");
        codigo.append("    public static void main(String[] args) {\n");
        codigo.append("        Scanner scanner = new Scanner(System.in);\n");
        
        for (Stmt stmt : statements) {
            codigo.append(stmt.accept(this));
        }
        
        codigo.append("    }\n");
        codigo.append("}\n");
        
        return codigo.toString();
    }


    @Override
    public String visitVar(Stmt.Var stmt) {
        String inicializacao = "";
        if (stmt.initializer != null) {
            inicializacao = " = " + stmt.initializer.accept(this);
        }
        String tipoJava = converterTipo(stmt.typeKeyword);
        return "        " + tipoJava + " " + stmt.name + inicializacao + ";\n";
    }

    @Override
    public String visitPrint(Stmt.Print stmt) {
        return "        System.out.println(" + stmt.expression.accept(this) + ");\n";
    }

    @Override
    public String visitIf(Stmt.If stmt) {
        String codigo = "        if (" + stmt.condition.accept(this) + ") {\n";
        codigo += stmt.thenBranch.accept(this);
        codigo += "        }";
        if (stmt.elseBranch != null) {
            codigo += " else {\n" + stmt.elseBranch.accept(this) + "        }";
        }
        return codigo + "\n";
    }

    @Override
    public String visitWhile(Stmt.While stmt) {
        return "        while (" + stmt.condition.accept(this) + ") " + stmt.body.accept(this) + "\n";
    }

    @Override
    public String visitBlock(Stmt.Block stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Stmt s : stmt.statements) {
            sb.append(s.accept(this));
        }
        sb.append("        }\n");
        return sb.toString();
    }

    @Override
    public String visitExprStmt(Stmt.ExprStmt stmt) {
        return "        " + stmt.expression.accept(this) + ";\n";
    }


    @Override
    public String visitBinary(Expr.Binary expr) {
        if (expr.operator.equals("==") || expr.operator.equals("!=")) {
        }
        return expr.left.accept(this) + " " + expr.operator + " " + expr.right.accept(this);
    }

    @Override
    public String visitVariable(Expr.Variable expr) {
        return expr.name;
    }

    @Override
    public String visitLiteral(Expr.Literal expr) {
        if (expr.value instanceof String) {
            return "\"" + expr.value + "\"";
        }
        return expr.value.toString();
    }
    
    @Override public String visitGrouping(Expr.Grouping expr) { return "(" + expr.expression.accept(this) + ")"; }
    @Override public String visitUnary(Expr.Unary expr) { return expr.operator + expr.right.accept(this); }
    @Override public String visitLogical(Expr.Logical expr) { return expr.left.accept(this) + " " + expr.operator + " " + expr.right.accept(this); }

    private String converterTipo(String tipoNossaLing) {
        if (tipoNossaLing.equals("real")) return "double";
        if (tipoNossaLing.equals("string")) return "String";
        return "int";
    }


@Override
    public String visitRead(Stmt.Read stmt) {
        return "        " + stmt.name + " = scanner.nextDouble();\n"; 
    }
}
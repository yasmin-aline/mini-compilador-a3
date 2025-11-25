package org.example.semantico;

import java.util.List;
import org.example.sintatico.Stmt;
import org.example.sintatico.Expr;

public class AnalisadorSemantico implements Stmt.Visitor<Void>, Expr.Visitor<Void> {

    private TabelaDeSimbolos tabela = new TabelaDeSimbolos();

    public void analisar(List<Stmt> statements) {
        for (Stmt statement : statements) {
            executar(statement);
        }
    }

    private void executar(Stmt stmt) {
        if (stmt != null) stmt.accept(this);
    }

    private void avaliar(Expr expr) {
        if (expr != null) expr.accept(this);
    }

    
    @Override
    public Void visitVar(Stmt.Var stmt) {
        String nomeVariavel = stmt.name;       
        String tipoVariavel = stmt.typeKeyword; 

        if (!tabela.adicionar(nomeVariavel, tipoVariavel)) {
            throw new RuntimeException("Erro Semântico: A variável '" + nomeVariavel + "' já foi declarada.");
        }

        if (stmt.initializer != null) {
            avaliar(stmt.initializer);
            
        }

        System.out.println("LOG: Variável declarada: " + nomeVariavel + " (" + tipoVariavel + ")");
        return null;
    }

    @Override
    public Void visitRead(Stmt.Read stmt) {
        if (!tabela.existe(stmt.name)) {
            throw new RuntimeException("Erro Semântico: A variável '" + stmt.name + "' não foi declarada antes de read.");
        }
        return null;
    }

    
    @Override
    public Void visitVariable(Expr.Variable expr) {
        // No arquivo Expr.java, 'name' já é uma String.
        if (!tabela.existe(expr.name)) {
            throw new RuntimeException("Erro Semântico: A variável '" + expr.name + "' não foi declarada.");
        }
        return null;
    }

    
    @Override
    public Void visitBinary(Expr.Binary expr) {
        if (expr.operator.equals("=")) {
            
            if (expr.left instanceof Expr.Variable) {
                String nomeVar = ((Expr.Variable) expr.left).name;
                if (!tabela.existe(nomeVar)) {
                     throw new RuntimeException("Erro Semântico: Tentativa de atribuir valor a variável não declarada '" + nomeVar + "'.");
                }
            } else {
                throw new RuntimeException("Erro Semântico: Atribuição inválida. O lado esquerdo deve ser uma variável.");
            }

            avaliar(expr.right);
            
        } else {
            avaliar(expr.left);
            avaliar(expr.right);
        }
        return null;
    }

    @Override
    public Void visitExprStmt(Stmt.ExprStmt stmt) {
        avaliar(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrint(Stmt.Print stmt) {
        avaliar(stmt.expression);
        return null;
    }

    @Override
    public Void visitBlock(Stmt.Block stmt) {

        for (Stmt s : stmt.statements) {
            executar(s);
        }
        return null;
    }

    @Override
    public Void visitIf(Stmt.If stmt) {
        avaliar(stmt.condition);
        executar(stmt.thenBranch);
        if (stmt.elseBranch != null) executar(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitWhile(Stmt.While stmt) {
        avaliar(stmt.condition);
        executar(stmt.body);
        return null;
    }

    @Override
    public Void visitLiteral(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitGrouping(Expr.Grouping expr) {
        avaliar(expr.expression);
        return null;
    }

    @Override
    public Void visitUnary(Expr.Unary expr) {
        avaliar(expr.right);
        return null;
    }

    @Override
    public Void visitLogical(Expr.Logical expr) {
        avaliar(expr.left);
        avaliar(expr.right);
        return null;
    }
}
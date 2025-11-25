package org.example.icg;

import org.example.sintatico.Expr;
import org.example.sintatico.Stmt;

import java.util.ArrayList;
import java.util.List;

public class GeradorDeCodigo implements Stmt.Visitor<Void>, Expr.Visitor<Void> {

    private final List<Instrucao> codigo = new ArrayList<>();
    private int labelCounter = 0;

    public List<Instrucao> gerar(List<Stmt> statements) {
        for (Stmt stmt : statements) {
            if (stmt != null) {
                stmt.accept(this);
            }
        }
        codigo.add(new Instrucao(Instrucao.OpCode.HALT));
        return codigo;
    }

    private void emit(Instrucao.OpCode opCode) {
        codigo.add(new Instrucao(opCode));
    }

    private void emit(Instrucao.OpCode opCode, Object operando) {
        codigo.add(new Instrucao(opCode, operando));
    }
    
    private void emitLabel(String label) {
        codigo.add(new Instrucao(Instrucao.OpCode.LABEL, label));
    }

    private String novaLabel() {
        return "L" + (labelCounter++);
    }

    @Override
    public Void visitVar(Stmt.Var stmt) {
        if (stmt.initializer != null) {
            stmt.initializer.accept(this);
            emit(Instrucao.OpCode.STORE, stmt.name);
        } else {
            emit(Instrucao.OpCode.PUSH, 0);
            emit(Instrucao.OpCode.STORE, stmt.name);
        }
        return null;
    }

    @Override
    public Void visitExprStmt(Stmt.ExprStmt stmt) {
        stmt.expression.accept(this);
        emit(Instrucao.OpCode.POP);
        return null;
    }

    @Override
    public Void visitPrint(Stmt.Print stmt) {
        stmt.expression.accept(this); 
        emit(Instrucao.OpCode.PRINT); 
        return null;
    }

    @Override
    public Void visitRead(Stmt.Read stmt) {
        emit(Instrucao.OpCode.READ, stmt.name); 
        return null;
    }

    @Override
    public Void visitBlock(Stmt.Block stmt) {
        for (Stmt s : stmt.statements) {
            if (s != null) {
                s.accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visitIf(Stmt.If stmt) {
        String labelElse = novaLabel();
        String labelFim = novaLabel();

        stmt.condition.accept(this);
        emit(Instrucao.OpCode.JMPF, labelElse);

        if (stmt.thenBranch != null) {
            stmt.thenBranch.accept(this);
        }

        emit(Instrucao.OpCode.JMP, labelFim);
        emitLabel(labelElse);

        if (stmt.elseBranch != null) {
            stmt.elseBranch.accept(this);
        }

        emitLabel(labelFim);
        return null;
    }

    @Override
    public Void visitWhile(Stmt.While stmt) {
        String labelInicio = novaLabel();
        String labelFim = novaLabel();

        emitLabel(labelInicio);

        stmt.condition.accept(this); 

        emit(Instrucao.OpCode.JMPF, labelFim);

        if (stmt.body != null) {
            stmt.body.accept(this);
        }

        emit(Instrucao.OpCode.JMP, labelInicio);

        emitLabel(labelFim);
        return null;
    }

    @Override
    public Void visitLiteral(Expr.Literal expr) {
        emit(Instrucao.OpCode.PUSH, expr.value); 
        return null;
    }

    @Override
    public Void visitVariable(Expr.Variable expr) {
        emit(Instrucao.OpCode.LOAD, expr.name); 
        return null;
    }

    @Override
    public Void visitGrouping(Expr.Grouping expr) {
        expr.expression.accept(this); 
        return null;
    }

    @Override
    public Void visitUnary(Expr.Unary expr) {
        expr.right.accept(this);
        switch (expr.operator) {
            case "-":
                emit(Instrucao.OpCode.NEG);
                break;
            case "!":
                emit(Instrucao.OpCode.NOT);
                break;
            default:
                throw new RuntimeException("Operador unário desconhecido: " + expr.operator);
        }
        return null;
    }

    @Override
    public Void visitBinary(Expr.Binary expr) {
        // Atribuição
        if ("=".equals(expr.operator)) {
            if (!(expr.left instanceof Expr.Variable)) {
                throw new RuntimeException("Lado esquerdo da atribuição deve ser variável.");
            }
            Expr.Variable var = (Expr.Variable) expr.left;
            expr.right.accept(this);
            emit(Instrucao.OpCode.STORE, var.name);
            emit(Instrucao.OpCode.LOAD, var.name); 
            return null;
        }

        expr.left.accept(this);
        expr.right.accept(this);

        switch (expr.operator) {
            case "+": emit(Instrucao.OpCode.ADD); break;
            case "-": emit(Instrucao.OpCode.SUB); break;
            case "*": emit(Instrucao.OpCode.MUL); break;
            case "/": emit(Instrucao.OpCode.DIV); break;
            case "%": emit(Instrucao.OpCode.MOD); break;
            case "==": emit(Instrucao.OpCode.EQ); break;
            case "!=": emit(Instrucao.OpCode.NEQ); break;
            case ">": emit(Instrucao.OpCode.GT); break;
            case "<": emit(Instrucao.OpCode.LT); break;
            case ">=": emit(Instrucao.OpCode.GTE); break;
            case "<=": emit(Instrucao.OpCode.LTE); break;
            default: throw new RuntimeException("Operador binário desconhecido na Geração de Código: " + expr.operator);
        }
        return null;
    }

    @Override
    public Void visitLogical(Expr.Logical expr) {
        String labelFim = novaLabel();
        
        expr.left.accept(this);
        
        if ("||".equals(expr.operator)) {
            emit(Instrucao.OpCode.JMPT, labelFim);
            expr.right.accept(this);
        } else if ("&&".equals(expr.operator)) {
            emit(Instrucao.OpCode.JMPF, labelFim);
            expr.right.accept(this);
        } else {
            throw new RuntimeException("Operador lógico desconhecido: " + expr.operator);
        }
        
        emitLabel(labelFim);
        return null;
    }
}

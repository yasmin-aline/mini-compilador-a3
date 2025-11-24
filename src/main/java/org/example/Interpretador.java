package org.example;

import org.example.sintatico.Expr;
import org.example.sintatico.Stmt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Interpretador implements Stmt.Visitor<Void>, Expr.Visitor<Object> {

    private final Map<String, Object> ambiente = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);

    public void executar(List<Stmt> statements) {
        for (Stmt stmt : statements) {
            if (stmt != null) {
                stmt.accept(this);
            }
        }
    }

    @Override
    public Void visitVar(Stmt.Var stmt) {
        Object valor = null;
        if (stmt.initializer != null) {
            valor = avaliar(stmt.initializer);
        }
        ambiente.put(stmt.name, valor);
        return null;
    }

    @Override
    public Void visitExprStmt(Stmt.ExprStmt stmt) {
        avaliar(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrint(Stmt.Print stmt) {
        Object valor = avaliar(stmt.expression);
        System.out.println(valor != null ? valor : "null");
        return null;
    }

    @Override
    public Void visitRead(Stmt.Read stmt) {
        System.out.print("[INPUT] Informe valor para " + stmt.name + ": ");
        String linha = scanner.nextLine();
        ambiente.put(stmt.name, linha);
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
        if (eVerdadeiro(avaliar(stmt.condition))) {
            if (stmt.thenBranch != null) stmt.thenBranch.accept(this);
        } else if (stmt.elseBranch != null) {
            stmt.elseBranch.accept(this);
        }
        return null;
    }

    @Override
    public Void visitWhile(Stmt.While stmt) {
        while (eVerdadeiro(avaliar(stmt.condition))) {
            if (stmt.body != null) stmt.body.accept(this);
        }
        return null;
    }

    @Override
    public Object visitLiteral(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitVariable(Expr.Variable expr) {
        if (!ambiente.containsKey(expr.name)) {
            throw new RuntimeException("Erro em tempo de execução: variável '" + expr.name + "' não inicializada.");
        }
        return ambiente.get(expr.name);
    }

    @Override
    public Object visitGrouping(Expr.Grouping expr) {
        return avaliar(expr.expression);
    }

    @Override
    public Object visitUnary(Expr.Unary expr) {
        Object right = avaliar(expr.right);

        if ("-".equals(expr.operator)) {
            Double v = paraNumero(right);
            return -v;
        }

        throw new RuntimeException("Operador unário desconhecido: " + expr.operator);
    }

    @Override
    public Object visitBinary(Expr.Binary expr) {
        // Atribuição: (variable = expr)
        if ("=".equals(expr.operator)) {
            if (!(expr.left instanceof Expr.Variable)) {
                throw new RuntimeException("Lado esquerdo da atribuição deve ser variável.");
            }
            String nome = ((Expr.Variable) expr.left).name;
            Object valor = avaliar(expr.right);
            ambiente.put(nome, valor);
            return valor;
        }

        Object left = avaliar(expr.left);
        Object right = avaliar(expr.right);

        switch (expr.operator) {
            case "+":
                return paraNumero(left) + paraNumero(right);
            case "-":
                return paraNumero(left) - paraNumero(right);
            case "*":
                return paraNumero(left) * paraNumero(right);
            case "/":
                return paraNumero(left) / paraNumero(right);
            case "%":
                return paraNumero(left) % paraNumero(right);
            case ">":
                return paraNumero(left) > paraNumero(right);
            case "<":
                return paraNumero(left) < paraNumero(right);
            case ">=":
                return paraNumero(left) >= paraNumero(right);
            case "<=":
                return paraNumero(left) <= paraNumero(right);
            case "==":
                return (left == null) ? right == null : left.equals(right);
            case "!=":
                return (left == null) ? right != null : !left.equals(right);
            default:
                throw new RuntimeException("Operador binário desconhecido: " + expr.operator);
        }
    }

    @Override
    public Object visitLogical(Expr.Logical expr) {
        Object left = avaliar(expr.left);

        if ("||".equals(expr.operator)) {
            if (eVerdadeiro(left)) return true;
            return eVerdadeiro(avaliar(expr.right));
        } else if ("&&".equals(expr.operator)) {
            if (!eVerdadeiro(left)) return false;
            return eVerdadeiro(avaliar(expr.right));
        }

        throw new RuntimeException("Operador lógico desconhecido: " + expr.operator);
    }

    private Object avaliar(Expr expr) {
        if (expr == null) return null;
        return expr.accept(this);
    }

    private boolean eVerdadeiro(Object valor) {
        if (valor == null) return false;
        if (valor instanceof Boolean) return (Boolean) valor;
        if (valor instanceof Number) return ((Number) valor).doubleValue() != 0.0;
        if (valor instanceof String) return !((String) valor).isEmpty();
        return true;
    }

    private Double paraNumero(Object valor) {
        if (valor instanceof Number) {
            return ((Number) valor).doubleValue();
        }
        if (valor instanceof String) {
            try {
                return Double.parseDouble((String) valor);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Valor não numérico: '" + valor + "'");
            }
        }
        throw new RuntimeException("Tipo não numérico: " + valor);
    }
}

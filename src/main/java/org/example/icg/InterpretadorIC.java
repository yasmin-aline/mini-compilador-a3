package org.example.icg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class InterpretadorIC {

    private final List<Instrucao> codigo;
    private final Map<String, Object> ambiente = new HashMap<>();
    private final Stack<Object> pilha = new Stack<>();
    private final Scanner scanner = new Scanner(System.in);

    public InterpretadorIC(List<Instrucao> codigo) {
        this.codigo = codigo;
    }

    public void executar() {
        int pc = 0; 
        while (pc < codigo.size()) {
            Instrucao instrucao = codigo.get(pc);
            pc++; 

            switch (instrucao.opCode) {
                case PUSH:
                    pilha.push(instrucao.operando);
                    break;
                case POP:
                    pilha.pop();
                    break;
                case LOAD:
                    String varLoad = (String) instrucao.operando;
                    if (!ambiente.containsKey(varLoad)) {
                        throw new RuntimeException("Erro em tempo de execução: Variável '" + varLoad + "' não inicializada.");
                    }
                    pilha.push(ambiente.get(varLoad));
                    break;
                case STORE: {
                    if (pilha.isEmpty()) {
                        throw new RuntimeException("Pilha vazia para operação STORE");
                    }
                    String varStore = (String) instrucao.operando;
                    ambiente.put(varStore, pilha.pop());
                    break;
                }
                case ADD: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação ADD");
                    }
                    double right = paraNumero(pilha.pop());
                    double left = paraNumero(pilha.pop());
                    pilha.push(left + right);
                    break;
                }
                case SUB: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação SUB");
                    }
                    double right = paraNumero(pilha.pop());
                    double left = paraNumero(pilha.pop());
                    pilha.push(left - right);
                    break;
                }
                case MUL: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação MUL");
                    }
                    double right = paraNumero(pilha.pop());
                    double left = paraNumero(pilha.pop());
                    pilha.push(left * right);
                    break;
                }
                case DIV: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação DIV");
                    }
                    double right = paraNumero(pilha.pop());
                    if (right == 0) {
                        throw new RuntimeException("Divisão por zero");
                    }
                    double left = paraNumero(pilha.pop());
                    pilha.push(left / right);
                    break;
                }
                case MOD: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação MOD");
                    }
                    double right = paraNumero(pilha.pop());
                    if (right == 0) {
                        throw new RuntimeException("Módulo por zero");
                    }
                    double left = paraNumero(pilha.pop());
                    pilha.push(left % right);
                    break;
                }
                case NEG: {
                    if (pilha.isEmpty()) {
                        throw new RuntimeException("Pilha vazia para operação NEG");
                    }
                    pilha.push(-paraNumero(pilha.pop()));
                    break;
                }
                case EQ: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação EQ");
                    }
                    Object right = pilha.pop();
                    Object left = pilha.pop();
                    pilha.push(left.equals(right));
                    break;
                }
                case NEQ: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação NEQ");
                    }
                    Object right = pilha.pop();
                    Object left = pilha.pop();
                    pilha.push(!left.equals(right));
                    break;
                }
                case GT: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação GT");
                    }
                    double rightGT = paraNumero(pilha.pop());
                    double leftGT = paraNumero(pilha.pop());
                    pilha.push(leftGT > rightGT);
                    break;
                }
                case LT: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação LT");
                    }
                    double rightLT = paraNumero(pilha.pop());
                    double leftLT = paraNumero(pilha.pop());
                    pilha.push(leftLT < rightLT);
                    break;
                }
                case GTE: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação GTE");
                    }
                    double rightGTE = paraNumero(pilha.pop());
                    double leftGTE = paraNumero(pilha.pop());
                    pilha.push(leftGTE >= rightGTE);
                    break;
                }
                case LTE: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação LTE");
                    }
                    double rightLTE = paraNumero(pilha.pop());
                    double leftLTE = paraNumero(pilha.pop());
                    pilha.push(leftLTE <= rightLTE);
                    break;
                }
                case AND: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação AND");
                    }
                    boolean right = eVerdadeiro(pilha.pop());
                    boolean left = eVerdadeiro(pilha.pop());
                    pilha.push(left && right);
                    break;
                }
                case OR: {
                    if (pilha.size() < 2) {
                        throw new RuntimeException("Pilha vazia para operação OR");
                    }
                    boolean right = eVerdadeiro(pilha.pop());
                    boolean left = eVerdadeiro(pilha.pop());
                    pilha.push(left || right);
                    break;
                }
                case NOT: {
                    if (pilha.isEmpty()) {
                        throw new RuntimeException("Pilha vazia para operação NOT");
                    }
                    pilha.push(!eVerdadeiro(pilha.pop()));
                    break;
                }
                case JMP:
                    pc = encontrarLabel((String) instrucao.operando);
                    break;
                case JMPT: {
                    if (pilha.isEmpty()) {
                        throw new RuntimeException("Pilha vazia para operação JMPT");
                    }
                    if (eVerdadeiro(pilha.peek())) {
                        pc = encontrarLabel((String) instrucao.operando);
                    } else {
                        pilha.pop();
                    }
                    break;
                }
                case JMPF: {
                    if (pilha.isEmpty()) {
                        throw new RuntimeException("Pilha vazia para operação JMPF");
                    }
                    if (!eVerdadeiro(pilha.peek())) {
                        pc = encontrarLabel((String) instrucao.operando);
                    } else {
                        pilha.pop();
                    }
                    break;
                }
                case READ:
                    String varRead = (String) instrucao.operando;
                    System.out.print("[INPUT] Informe valor para " + varRead + ": ");
                    String linha = scanner.nextLine();
                    try {
                        ambiente.put(varRead, Double.parseDouble(linha));
                    } catch (NumberFormatException e) {
                        ambiente.put(varRead, linha);
                    }
                    break;
                case PRINT: {
                    if (pilha.isEmpty()) {
                        throw new RuntimeException("Pilha vazia para operação PRINT");
                    }
                    System.out.println(pilha.pop());
                    break;
                }
                case HALT:
                    System.out.println("[INFO] Execução do Código Intermediário finalizada.");
                    return;
                case LABEL:
                    break;
                default:
                    throw new RuntimeException("OpCode desconhecido: " + instrucao.opCode);
            }
        }
    }

    private int encontrarLabel(String label) {
        for (int i = 0; i < codigo.size(); i++) {
            Instrucao inst = codigo.get(i);
            if (inst.opCode == Instrucao.OpCode.LABEL && label.equals(inst.operando)) {
                return i + 1; 
            }
        }
        throw new RuntimeException("Label não encontrada: " + label);
    }

    private double paraNumero(Object valor) {
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

    private boolean eVerdadeiro(Object valor) {
        if (valor == null) return false;
        if (valor instanceof Boolean) return (Boolean) valor;
        if (valor instanceof Number) return ((Number) valor).doubleValue() != 0.0;
        if (valor instanceof String) return !((String) valor).isEmpty();
        return true;
    }
}
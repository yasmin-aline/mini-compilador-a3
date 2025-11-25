package org.example.icg;

public class Instrucao {
    public enum OpCode {
        PUSH, POP,
        LOAD, STORE,
        ADD, SUB, MUL, DIV, MOD,
        NEG,  // Operador unário de negação
        EQ, NEQ, GT, LT, GTE, LTE,
        AND, OR, NOT,
        JMP, JMPT, JMPF,
        LABEL,  // Marcador de posição para saltos
        READ, PRINT,
        HALT
    }

    public final OpCode opCode;
    public final Object operando;

    public Instrucao(OpCode opCode) {
        this(opCode, null);
    }

    public Instrucao(OpCode opCode, Object operando) {
        this.opCode = opCode;
        this.operando = operando;
    }

    @Override
    public String toString() {
        if (operando != null) {
            return String.format("%-8s %s", opCode, operando);
        }
        return opCode.toString();
    }
}
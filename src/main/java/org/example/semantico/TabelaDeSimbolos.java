package org.example.semantico;

import java.util.HashMap;
import java.util.Map;

public class TabelaDeSimbolos {

    private final Map<String, String> simbolos = new HashMap<>();

    public boolean adicionar(String nome, String tipo) {
        if (simbolos.containsKey(nome)) {
            return false;
        }
        simbolos.put(nome, tipo);
        return true;
    }

    public boolean existe(String nome) {
        return simbolos.containsKey(nome);
    }

    public String getTipo(String nome) {
        return simbolos.get(nome);
    }
}

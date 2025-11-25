package org.example.semantico;

import java.util.HashMap;
import java.util.Map;

public class TabelaDeSimbolos {
    private Map<String, String> tabela = new HashMap<>();

        public boolean adicionar(String nome, String tipo) {
        if (tabela.containsKey(nome)) {
            return false; 
        }
        tabela.put(nome, tipo);
        return true; 
    }

        public boolean existe(String nome) {
        return tabela.containsKey(nome);
    }

        public String getTipo(String nome) {
        return tabela.get(nome);
    }
}
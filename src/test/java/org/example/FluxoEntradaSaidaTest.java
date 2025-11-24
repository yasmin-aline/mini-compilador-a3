package org.example;

import org.example.lexico.AnalisadorLexico;
import org.example.sintatico.Parser;
import org.example.sintatico.Stmt;
import org.example.semantico.AnalisadorSemantico;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FluxoEntradaSaidaTest {

    private List<Stmt> compilar(String codigo) {
        var tokens = new AnalisadorLexico(codigo).analisar();
        Parser parser = new Parser(tokens);
        List<Stmt> programa = parser.analisar();

        AnalisadorSemantico semantico = new AnalisadorSemantico();
        semantico.analisar(programa);

        return programa;
    }

    @Test
    void fluxoCompletoComReadEPrint() {
        String codigo = """
                int x = 0;
                read(x);
                print(x);
                """;

        List<Stmt> programa = compilar(codigo);

        ByteArrayInputStream entradaFalsa = new ByteArrayInputStream("42\n".getBytes());
        InputStream entradaOriginal = System.in;

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PrintStream saidaOriginal = System.out;

        System.setIn(entradaFalsa);
        System.setOut(new PrintStream(saida));
        try {
            Interpretador interpretador = new Interpretador();
            interpretador.executar(programa);
        } finally {
            System.setIn(entradaOriginal);
            System.setOut(saidaOriginal);
        }

        String esperado = "[INPUT] Informe valor para x: 42" + System.lineSeparator();
        assertEquals(esperado, saida.toString());
    }
}

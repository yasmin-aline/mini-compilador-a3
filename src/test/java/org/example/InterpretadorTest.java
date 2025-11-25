package org.example;

import org.example.lexico.AnalisadorLexico;
import org.example.sintatico.Parser;
import org.example.sintatico.Stmt;
import org.example.semantico.AnalisadorSemantico;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterpretadorTest {

    private List<Stmt> compilar(String codigo) {
        AnalisadorLexico lexer = new AnalisadorLexico(codigo);
        var tokens = lexer.analisar();

        Parser parser = new Parser(tokens);
        List<Stmt> programa = parser.analisar();

        AnalisadorSemantico semantico = new AnalisadorSemantico();
        semantico.analisar(programa);

        return programa;
    }

    @Test
    void executaWhileEIfSimples() {
        String codigo = """
                int x = 0;
                while (x < 3) {
                    if (x == 1) {
                        print(x);
                    } else {
                        print(x);
                    }
                    x = x + 1;
                }
                """;

        List<Stmt> programa = compilar(codigo);

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PrintStream antigaSaida = System.out;
        System.setOut(new PrintStream(saida));
        try {
            Interpretador interpretador = new Interpretador();
            interpretador.executar(programa);
        } finally {
            System.setOut(antigaSaida);
        }

        String esperado = String.join(System.lineSeparator(),
                "0",
                "1.0",
                "2.0",
                "") ;

        assertEquals(esperado, saida.toString());
    }
}

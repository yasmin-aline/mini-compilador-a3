package org.example;

import org.example.lexico.AnalisadorLexico;
import org.example.sintatico.Parser;
import org.example.sintatico.Stmt;
import org.example.semantico.AnalisadorSemantico;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SemanticoTest {

    private List<Stmt> parse(String codigo) {
        var tokens = new AnalisadorLexico(codigo).analisar();
        Parser parser = new Parser(tokens);
        return parser.analisar();
    }

    @Test
    void erroAoRedeclararVariavel() {
        String codigo = """
                int x = 1;
                int x = 2;
                """;

        List<Stmt> stmts = parse(codigo);
        AnalisadorSemantico semantico = new AnalisadorSemantico();

        assertThrows(RuntimeException.class, () -> semantico.analisar(stmts));
    }

    @Test
    void erroAoUsarVariavelNaoDeclaradaEmPrint() {
        String codigo = """
                print(y);
                """;

        List<Stmt> stmts = parse(codigo);
        AnalisadorSemantico semantico = new AnalisadorSemantico();

        assertThrows(RuntimeException.class, () -> semantico.analisar(stmts));
    }

    @Test
    void erroAoAtribuirParaAlgoQueNaoEhVariavel() {
        String codigo = """
                1 = 2;
                """;

        assertThrows(RuntimeException.class, () -> parse(codigo));
    }
}

package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.example.lexico.AnalisadorLexico;
import org.example.sintatico.Parser;
import org.example.sintatico.Stmt;
import org.example.semantico.AnalisadorSemantico;
import org.example.gerador.GeradorCodigo;

public class IntegracaoTest {

    @Test
    public void testeFluxoCompleto() {
        String codigo = "int x = 10; int y = 20; if (x < y) { print(x); }";

        AnalisadorLexico lexico = new AnalisadorLexico(codigo);
        var tokens = lexico.analisar();
        assertFalse(tokens.isEmpty(), "Deveria ter gerado tokens");

        Parser parser = new Parser(tokens);
        List<Stmt> ast = parser.analisar();
        assertFalse(ast.isEmpty(), "Deveria ter gerado AST");

        AnalisadorSemantico semantico = new AnalisadorSemantico();
        assertDoesNotThrow(() -> semantico.analisar(ast), "Semântico não deveria falhar para código válido");

        GeradorCodigo gerador = new GeradorCodigo();
        String javaCode = gerador.gerarClasseJava(ast);
        
        assertTrue(javaCode.contains("public class ProgramaCompilado"), "Deve ter a classe principal");
        assertTrue(javaCode.contains("int x = 10;"), "Deve ter a declaração de x");
        assertTrue(javaCode.contains("System.out.println"), "Deve ter traduzido print para System.out");
    }

    @Test
    public void testeErroSemantico() {
        String codigo = "print(w);"; 

        AnalisadorLexico lexico = new AnalisadorLexico(codigo);
        Parser parser = new Parser(lexico.analisar());
        List<Stmt> ast = parser.analisar();
        
        AnalisadorSemantico semantico = new AnalisadorSemantico();


        assertThrows(RuntimeException.class, () -> {
            semantico.analisar(ast);
        });
    }
}
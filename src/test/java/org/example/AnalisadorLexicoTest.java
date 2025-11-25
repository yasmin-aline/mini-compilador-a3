package org.example;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.example.lexico.AnalisadorLexico;

public class AnalisadorLexicoTest {
    
    @Test
    public void testAnaliseLexicaBasica() {
        String codigo = """
            int x = 10;
            real y = 2.5;
            string nome = "teste";
            
            if (x > 5) {
                print("Maior que 5");
            } else {
                print("Menor ou igual a 5");
            }
            
            while (x > 0) {
                x = x - 1;
            }
            """;
            
        AnalisadorLexico lexer = new AnalisadorLexico(codigo);
        List<AnalisadorLexico.Token> tokens = lexer.analisar();
        
        // Verifica se existem tokens
        assertFalse(tokens.isEmpty(), "A lista de tokens não deve estar vazia");
        
        // Verifica se o último token é EOF
        assertEquals(AnalisadorLexico.TipoToken.EOF, 
                    tokens.get(tokens.size() - 1).tipo,
                    "O último token deve ser EOF");
        
        // Imprime os tokens para depuração
        System.out.println("\nTokens encontrados (" + tokens.size() + "):");
        for (AnalisadorLexico.Token token : tokens) {
            System.out.println(token);
        }
    }
    
    @Test
    public void testAnalisarArquivo() {
        // Caminho para um arquivo de teste
        String caminhoArquivo = "src/test/resources/exemplo.txt";
        
        try {
            List<AnalisadorLexico.Token> tokens = 
                AnalisadorLexico.analisarArquivo(caminhoArquivo);
                
            assertFalse(tokens.isEmpty(), "A lista de tokens não deve estar vazia");
            System.out.println("\nTokens do arquivo (" + tokens.size() + "):");
            tokens.forEach(System.out::println);
            
        } catch (Exception e) {
            fail("Falha ao analisar o arquivo: " + e.getMessage());
        }
    }
}

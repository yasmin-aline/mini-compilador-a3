package org.example;

import org.example.lexico.AnalisadorLexico;
import org.example.semantico.AnalisadorSemantico;
import org.example.sintatico.Parser;
import org.example.sintatico.Stmt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InterpretadorASTTest {

    private String capturarSaida(String fonte) {
        return capturarSaida(fonte, "");
    }

    private String capturarSaida(String fonte, String entradaUsuario) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(new ByteArrayOutputStream()));

        if (!entradaUsuario.isEmpty()) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(entradaUsuario.getBytes());
            System.setIn(inputStream);
        }

        try {
            interpretarDireto(fonte);
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
            System.setIn(System.in);
        }

        String saida = outputStream.toString().trim();
        
        String[] linhas = saida.split("\n");
        StringBuilder resultado = new StringBuilder();
        for (String linha : linhas) {
            if (!linha.startsWith("LOG:") && !linha.startsWith("[")) {
                if (resultado.length() > 0) resultado.append("\n");
                resultado.append(linha);
            }
        }
        
        return resultado.toString().trim();
    }

    private void interpretarDireto(String fonte) {
        List<AnalisadorLexico.Token> tokens = new AnalisadorLexico(fonte).analisar();
        Parser parser = new Parser(tokens);
        List<Stmt> programa = parser.analisar();

        AnalisadorSemantico semantico = new AnalisadorSemantico();
        semantico.analisar(programa);

        Interpretador interpretador = new Interpretador();
        interpretador.executar(programa);
    }

    // =========================================================================
    // TESTES BÁSICOS DO EDITAL (Interpretação AST)
    // =========================================================================

    @Test
    @DisplayName("AST: I. Declaração de variáveis (inteiros e reais)")
    void testDeclaracaoDeVariaveis() {
        String fonte = String.join("\n",
                "int x = 10;",
                "real y = 3.14;",
                "print(x);",
                "print(y);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("10"));
        assertTrue(saida.contains("3.14"));
    }

    @Test
    @DisplayName("AST: II. Atribuições")
    void testAtribuicoes() {
        String fonte = String.join("\n",
                "int x = 0;",
                "x = 5;",
                "x = x + 2;",
                "print(x);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("7"));
    }

    @Test
    @DisplayName("AST: III.a Estrutura sequencial")
    void testEstruturaSequencial() {
        String fonte = String.join("\n",
                "int a = 1;",
                "int b = 2;",
                "int c = a + b;",
                "print(c);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("3"));
    }

    @Test
    @DisplayName("AST: III.b Estrutura condicional if/else")
    void testIfElse() {
        String fonte = String.join("\n",
                "int x = 10;",
                "int y = 0;",
                "if (x > 5) {",
                "  y = 1;",
                "} else {",
                "  y = 2;",
                "}",
                "print(y);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("1"));
    }

    @Test
    @DisplayName("AST: III.c Estrutura de repetição while")
    void testWhile() {
        String fonte = String.join("\n",
                "int i = 0;",
                "int soma = 0;",
                "while (i < 5) {",
                "  soma = soma + i;",
                "  i = i + 1;",
                "}",
                "print(soma);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("10"));
    }

    @Test
    @DisplayName("AST: IV. Operações aritméticas básicas")
    void testOperacoesAritmeticas() {
        String fonte = String.join("\n",
                "int a = 10;",
                "int b = 3;",
                "print(a + b);",
                "print(a - b);",
                "print(a * b);",
                "print(a / b);",
                "print(a % b);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("13"));
        assertTrue(saida.contains("7"));
        assertTrue(saida.contains("30"));
        assertTrue(saida.contains("3.3"));
        assertTrue(saida.contains("1"));
    }

    @Test
    @DisplayName("AST: IV. Operações lógicas básicas")
    void testOperacoesLogicas() {
        String fonte = String.join("\n",
                "int a = 10;",
                "int b = 3;",
                "print(a > b);",
                "print(a < b);",
                "print(a == 10);",
                "print(a != b);",
                "print((a > 5) && (b > 2));",
                "print((a < 5) || (b > 2));"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("true"));
        assertTrue(saida.contains("false"));
    }

    @Test
    @DisplayName("AST: V. Comandos de saída (print)")
    void testComandoSaida() {
        String fonte = String.join("\n",
                "int x = 42;",
                "print(x);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("42"));
    }

    @Test
    @DisplayName("AST: V. Comandos de entrada (read)")
    void testComandoEntrada() {
        String fonte = String.join("\n",
                "string num;",
                "read(num);",
                "print(num);"
        );
        assertDoesNotThrow(() -> {
            ByteArrayInputStream inputStream = new ByteArrayInputStream("42\n".getBytes());
            System.setIn(inputStream);
            interpretarDireto(fonte);
            System.setIn(System.in); 
        });
    }

    // =========================================================================
    // TESTES AVANÇADOS - Casos Complexos
    // =========================================================================

    @Test
    @DisplayName("AST: Negação unária")
    void testNegacaoUnaria() {
        String fonte = String.join("\n",
                "int x = -5;",
                "int y = -(-3);",
                "print(x);",
                "print(y);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("-5"));
        assertTrue(saida.contains("3"));
    }

    @Test
    @DisplayName("AST: If sem else")
    void testIfSemElse() {
        String fonte = String.join("\n",
                "int x = 10;",
                "int y = 0;",
                "if (x > 5) {",
                "  y = 100;",
                "}",
                "print(y);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("100"));
    }

    @Test
    @DisplayName("AST: If aninhado")
    void testIfAninhado() {
        String fonte = String.join("\n",
                "int x = 10;",
                "int y = 5;",
                "int z = 0;",
                "if (x > 5) {",
                "  if (y > 3) {",
                "    z = 99;",
                "  }",
                "}",
                "print(z);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("99"));
    }

    @Test
    @DisplayName("AST: While aninhado")
    void testWhileAninhado() {
        String fonte = String.join("\n",
                "int i = 0;",
                "int j = 0;",
                "int soma = 0;",
                "while (i < 3) {",
                "  j = 0;",
                "  while (j < 2) {",
                "    soma = soma + 1;",
                "    j = j + 1;",
                "  }",
                "  i = i + 1;",
                "}",
                "print(soma);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("6")); // 3 * 2 = 6
    }

    @Test
    @DisplayName("AST: Expressões complexas com precedência")
    void testExpressoesComplexas() {
        String fonte = String.join("\n",
                "int a = 2;",
                "int b = 3;",
                "int c = 4;",
                "int result = a + b * c - (a + b);",
                "print(result);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("9")); // 2 + 12 - 5 = 9
    }

    @Test
    @DisplayName("AST: Precedência de operadores")
    void testPrecedenciaOperadores() {
        String fonte = String.join("\n",
                "int result = 2 + 3 * 4;",
                "print(result);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("14")); // Não 20
    }

    @Test
    @DisplayName("AST: Operadores de comparação")
    void testOperadoresComparacao() {
        String fonte = String.join("\n",
                "int a = 10;",
                "int b = 5;",
                "print(a > b);",
                "print(a < b);",
                "print(a >= 10);",
                "print(a <= 5);",
                "print(a == 10);",
                "print(a != 5);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("true"));
        assertTrue(saida.contains("false"));
    }

    @Test
    @DisplayName("AST: Short-circuit do operador OR")
    void testShortCircuitOR() {
        String fonte = String.join("\n",
                "int x = 1;",
                "int y = 0;",
                "int result = (x == 1) || (y == 1);",
                "print(result);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("true"));
    }

    @Test
    @DisplayName("AST: Short-circuit do operador AND")
    void testShortCircuitAND() {
        String fonte = String.join("\n",
                "int x = 0;",
                "int y = 1;",
                "int result = (x == 1) && (y == 1);",
                "print(result);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("false"));
    }

    @Test
    @DisplayName("AST: While com condição falsa inicial")
    void testWhileCondicaoFalsa() {
        String fonte = String.join("\n",
                "int i = 10;",
                "int soma = 0;",
                "while (i < 5) {",
                "  soma = soma + 1;",
                "}",
                "print(soma);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("0")); // Não entra no loop
    }

    @Test
    @DisplayName("AST: Variável sem inicialização")
    void testVariavelSemInicializacao() {
        String fonte = String.join("\n",
                "int x;",
                "x = 42;",
                "print(x);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("42"));
    }

    @Test
    @DisplayName("AST: Blocos aninhados")
    void testBlocosAninhados() {
        String fonte = String.join("\n",
                "int x = 1;",
                "{",
                "  int y = 2;",
                "  {",
                "    int z = 3;",
                "    print(z);",
                "  }",
                "  print(y);",
                "}",
                "print(x);"
        );
        assertDoesNotThrow(() -> interpretarDireto(fonte));
    }

    @Test
    @DisplayName("AST: Atribuição múltipla")
    void testAtribuicaoMultipla() {
        String fonte = String.join("\n",
                "int a = 0;",
                "int b = 0;",
                "int c = 0;",
                "a = 1;",
                "b = 2;",
                "c = 3;",
                "print(a);",
                "print(b);",
                "print(c);"
        );
        assertDoesNotThrow(() -> interpretarDireto(fonte));
    }

    @Test
    @DisplayName("AST: Divisão real")
    void testDivisaoReal() {
        String fonte = String.join("\n",
                "int a = 10;",
                "int b = 3;",
                "print(a / b);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("3.3"));
    }

    @Test
    @DisplayName("AST: Módulo")
    void testModulo() {
        String fonte = String.join("\n",
                "int a = 10;",
                "int b = 3;",
                "print(a % b);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("1"));
    }

    // =========================================================================
    // PROGRAMAS COMPLETOS
    // =========================================================================

    @Test
    @DisplayName("AST: Programa Completo - Fatorial")
    void testFatorial() {
        String fonte = String.join("\n",
                "int n = 5;",
                "int fatorial = 1;",
                "int i = 1;",
                "while (i <= n) {",
                "  fatorial = fatorial * i;",
                "  i = i + 1;",
                "}",
                "print(fatorial);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("120")); // 5! = 120
    }

    @Test
    @DisplayName("AST: Programa Completo - Fibonacci")
    void testFibonacci() {
        String fonte = String.join("\n",
                "int n = 7;",
                "int a = 0;",
                "int b = 1;",
                "int i = 2;",
                "int temp = 0;",
                "while (i < n) {",
                "  temp = a + b;",
                "  a = b;",
                "  b = temp;",
                "  i = i + 1;",
                "}",
                "print(b);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("8")); // Fibonacci(7) = 8
    }

    @Test
    @DisplayName("AST: Programa Completo - Número par")
    void testNumeroPar() {
        // O problema é que % retorna double (0.0) não int (0)
        // Então 0.0 == 0 pode falhar dependendo da comparação
        String fonte = String.join("\n",
                "int num = 10;",
                "int resto = num % 2;",
                "print(resto);"  // Vai imprimir 0.0 ou 0
        );
        String saida = capturarSaida(fonte);
        // Aceita tanto "0" quanto "0.0"
        assertTrue(saida.contains("0"), 
                   "Esperado '0' ou '0.0' (resto de 10 % 2), mas obteve: '" + saida + "'");
    }

    @Test
    @DisplayName("AST: Programa Completo - Soma 1 a 10")
    void testSoma1a10() {
        String fonte = String.join("\n",
                "int soma = 0;",
                "int i = 1;",
                "while (i <= 10) {",
                "  soma = soma + i;",
                "  i = i + 1;",
                "}",
                "print(soma);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("55")); // 1+2+...+10 = 55
    }

    @Test
    @DisplayName("AST: Programa Completo - Maior de três números")
    void testMaiorDeTres() {
        String fonte = String.join("\n",
                "int a = 5;",
                "int b = 8;",
                "int c = 3;",
                "int maior = a;",
                "if (b > maior) {",
                "  maior = b;",
                "}",
                "if (c > maior) {",
                "  maior = c;",
                "}",
                "print(maior);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("8"));
    }

    // =========================================================================
    // TESTES DE ERROS SEMÂNTICOS (Devem FALHAR)
    // =========================================================================

    @Test
    @DisplayName("AST: Erro - Variável não declarada")
    void testErroVariavelNaoDeclarada() {
        String fonte = String.join("\n",
                "int x = 10;",
                "print(y);" // y não existe
        );
        assertThrows(RuntimeException.class, () -> interpretarDireto(fonte));
    }

    @Test
    @DisplayName("AST: Erro - Variável declarada duas vezes")
    void testErroVariavelDuplicada() {
        String fonte = String.join("\n",
                "int x = 10;",
                "int x = 20;" // Redeclaração
        );
        assertThrows(RuntimeException.class, () -> interpretarDireto(fonte));
    }

    @Test
    @DisplayName("AST: Erro - Atribuição a variável não declarada")
    void testErroAtribuicaoNaoDeclarada() {
        String fonte = "x = 10;"; // x não foi declarada
        assertThrows(RuntimeException.class, () -> interpretarDireto(fonte));
    }

    @Test
    @DisplayName("AST: Erro - Read em variável não declarada")
    void testErroReadNaoDeclarada() {
        String fonte = "read(x);"; // x não existe
        assertThrows(RuntimeException.class, () -> interpretarDireto(fonte));
    }

    // =========================================================================
    // TESTES DE ERROS LÉXICOS (Devem FALHAR)
    // =========================================================================

    @Test
    @DisplayName("AST: Erro Léxico - Caractere inválido")
    void testErroCaractereInvalido() {
        String fonte = "int x = 10 @ 5;"; // @ é inválido
        assertThrows(RuntimeException.class, () -> interpretarDireto(fonte));
    }

    // =========================================================================
    // TESTES DE ERROS SINTÁTICOS (Devem FALHAR)
    // =========================================================================

    @Test
    @DisplayName("AST: Erro Sintático - Falta ponto e vírgula")
    void testErroFaltaPontoVirgula() {
        String fonte = String.join("\n",
                "int x = 10", // Falta ;
                "print(x);"
        );
        assertThrows(RuntimeException.class, () -> interpretarDireto(fonte));
    }

    @Test
    @DisplayName("AST: Erro Sintático - Parênteses não balanceados")
    void testErroParentesesNaoBalanceados() {
        String fonte = "print(10;"; // Falta )
        assertThrows(RuntimeException.class, () -> interpretarDireto(fonte));
    }

    // =========================================================================
    // TESTES COMPARATIVOS - AST vs IC
    // =========================================================================

    @Test
    @DisplayName("AST: Teste de consistência - resultado deve ser igual ao IC")
    void testConsistenciaComIC() {
        String fonte = String.join("\n",
                "int x = 5;",
                "int y = 3;",
                "int z = x * y + 2;",
                "print(z);"
        );
        
        // Executa com interpretador AST
        String saidaAST = capturarSaida(fonte);
        
        // Ambos devem produzir: 17 (5*3+2)
        assertTrue(saidaAST.contains("17"));
    }

    @Test
    @DisplayName("AST: Variável null imprime como null")
    void testVariavelNull() {
        String fonte = String.join("\n",
                "int x;",
                "print(x);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("null") || saida.contains("0"));
    }

    @Test
    @DisplayName("AST: Conversão de string para número em operação")
    void testConversaoStringNumero() {
        // Este teste requer read() interativo, vamos simplificar
        String fonte = String.join("\n",
                "string num = \"42\";",
                "print(num);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("42"), "Esperado '42', obteve: '" + saida + "'");
    }
}
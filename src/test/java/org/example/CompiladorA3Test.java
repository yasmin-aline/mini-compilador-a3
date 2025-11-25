package org.example;

import org.example.icg.GeradorDeCodigo;
import org.example.icg.Instrucao;
import org.example.icg.InterpretadorIC;
import org.example.lexico.AnalisadorLexico;
import org.example.semantico.AnalisadorSemantico;
import org.example.sintatico.Parser;
import org.example.sintatico.Stmt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompiladorA3Test {

    private String capturarSaida(String fonte) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            compilarEExecutar(fonte);
        } finally {
            System.setOut(originalOut);
        }

        return outputStream.toString().trim();
    }

    private void compilarEExecutar(String fonte) {
        List<AnalisadorLexico.Token> tokens = new AnalisadorLexico(fonte).analisar();
        Parser parser = new Parser(tokens);
        List<Stmt> programa = parser.analisar();

        AnalisadorSemantico semantico = new AnalisadorSemantico();
        semantico.analisar(programa);

        GeradorDeCodigo gerador = new GeradorDeCodigo();
        List<Instrucao> codigo = gerador.gerar(programa);

        InterpretadorIC interpretador = new InterpretadorIC(codigo);
        interpretador.executar();
    }

    // =========================================================================
    // TESTES DO EDITAL (Básicos - já existentes)
    // =========================================================================

    @Test
    @DisplayName("I. Declaração de variáveis (inteiros e reais)")
    void testDeclaracaoDeVariaveis() {
        String fonte = String.join("\n",
                "int x = 10;",
                "real y = 3.14;",
                "print(x);",
                "print(y);"
        );
        assertDoesNotThrow(() -> compilarEExecutar(fonte));
    }

    @Test
    @DisplayName("II. Atribuições")
    void testAtribuicoes() {
        String fonte = String.join("\n",
                "int x = 0;",
                "x = 5;",
                "x = x + 2;",
                "print(x);"
        );
        assertDoesNotThrow(() -> compilarEExecutar(fonte));
    }

    @Test
    @DisplayName("III.a Estrutura sequencial")
    void testEstruturaSequencial() {
        String fonte = String.join("\n",
                "int a = 1;",
                "int b = 2;",
                "int c = a + b;",
                "print(c);"
        );
        assertDoesNotThrow(() -> compilarEExecutar(fonte));
    }

    @Test
    @DisplayName("III.b Estrutura condicional if/else")
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
        assertDoesNotThrow(() -> compilarEExecutar(fonte));
    }

    @Test
    @DisplayName("III.c Estrutura de repetição while")
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
        assertDoesNotThrow(() -> compilarEExecutar(fonte));
    }

    @Test
    @DisplayName("IV. Operações aritméticas e lógicas básicas")
    void testOperacoesAritmeticasELogicas() {
        String fonte = String.join("\n",
                "int a = 10;",
                "int b = 3;",
                "int c = a + b * 2;",
                "int d = a / b;",
                "int e = a % b;",
                "int f = (a > b) && (b < 5);",
                "int g = (a == 10) || (b == 0);",
                "print(c);",
                "print(d);",
                "print(e);",
                "print(f);",
                "print(g);"
        );
        assertDoesNotThrow(() -> compilarEExecutar(fonte));
    }

    @Test
    @DisplayName("V. Comandos de entrada e saída")
    void testEntradaESaida() {
        String fonte = String.join("\n",
                "string nome;",
                "print(nome);"
        );
        assertDoesNotThrow(() -> compilarEExecutar(fonte));
    }

    // =========================================================================
    // TESTES AVANÇADOS - Casos Extremos e Edge Cases
    // =========================================================================

    @Test
    @DisplayName("Teste: Negação unária")
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
    @DisplayName("Teste: If sem else")
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
    @DisplayName("Teste: If aninhado")
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
    @DisplayName("Teste: While aninhado")
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
    @DisplayName("Teste: Expressões complexas")
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
    @DisplayName("Teste: Operadores de comparação")
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
    @DisplayName("Teste: Short-circuit do operador OR")
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
    @DisplayName("Teste: Short-circuit do operador AND")
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
    @DisplayName("Teste: Atribuição múltipla")
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
        assertDoesNotThrow(() -> compilarEExecutar(fonte));
    }

    @Test
    @DisplayName("Teste: Precedência de operadores")
    void testPrecedenciaOperadores() {
        String fonte = String.join("\n",
                "int result = 2 + 3 * 4;",
                "print(result);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("14")); // Não 20
    }

    @Test
    @DisplayName("Teste: Divisão inteira")
    void testDivisaoInteira() {
        String fonte = String.join("\n",
                "int a = 10;",
                "int b = 3;",
                "int result = a / b;",
                "print(result);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("3.3")); // Divisão real
    }

    @Test
    @DisplayName("Teste: Módulo")
    void testModulo() {
        String fonte = String.join("\n",
                "int a = 10;",
                "int b = 3;",
                "int result = a % b;",
                "print(result);"
        );
        String saida = capturarSaida(fonte);
        assertTrue(saida.contains("1"));
    }

    @Test
    @DisplayName("Teste: While com condição falsa inicial")
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
    @DisplayName("Teste: Variável sem inicialização")
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
    @DisplayName("Teste: Blocos aninhados")
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
        assertDoesNotThrow(() -> compilarEExecutar(fonte));
    }

    // =========================================================================
    // TESTES DE ANÁLISE SEMÂNTICA (Devem FALHAR)
    // =========================================================================

    @Test
    @DisplayName("Erro Semântico: Variável não declarada")
    void testErroVariavelNaoDeclarada() {
        String fonte = String.join("\n",
                "int x = 10;",
                "print(y);" // y não existe
        );
        assertThrows(RuntimeException.class, () -> compilarEExecutar(fonte));
    }

    @Test
    @DisplayName("Erro Semântico: Variável declarada duas vezes")
    void testErroVariavelDuplicada() {
        String fonte = String.join("\n",
                "int x = 10;",
                "int x = 20;" // Redeclaração
        );
        assertThrows(RuntimeException.class, () -> compilarEExecutar(fonte));
    }

    @Test
    @DisplayName("Erro Semântico: Atribuição a variável não declarada")
    void testErroAtribuicaoNaoDeclarada() {
        String fonte = String.join("\n",
                "x = 10;" // x não foi declarada
        );
        assertThrows(RuntimeException.class, () -> compilarEExecutar(fonte));
    }

    @Test
    @DisplayName("Erro Semântico: Read em variável não declarada")
    void testErroReadNaoDeclarada() {
        String fonte = "read(x);"; // x não existe
        assertThrows(RuntimeException.class, () -> compilarEExecutar(fonte));
    }

    // =========================================================================
    // TESTES DE ANÁLISE LÉXICA (Devem FALHAR)
    // =========================================================================

    @Test
    @DisplayName("Erro Léxico: Caractere inválido")
    void testErroCaractereInvalido() {
        String fonte = "int x = 10 @ 5;"; // @ é inválido
        assertThrows(RuntimeException.class, () -> compilarEExecutar(fonte));
    }

    // =========================================================================
    // TESTES DE ANÁLISE SINTÁTICA (Devem FALHAR)
    // =========================================================================

    @Test
    @DisplayName("Erro Sintático: Falta ponto e vírgula")
    void testErroFaltaPontoVirgula() {
        String fonte = String.join("\n",
                "int x = 10", // Falta ;
                "print(x);"
        );
        assertThrows(RuntimeException.class, () -> compilarEExecutar(fonte));
    }

    @Test
    @DisplayName("Erro Sintático: Parênteses não balanceados")
    void testErroParentesesNaoBalanceados() {
        String fonte = "print(10;"; // Falta )
        assertThrows(RuntimeException.class, () -> compilarEExecutar(fonte));
    }

    // =========================================================================
    // TESTE DE PROGRAMA COMPLETO
    // =========================================================================

    @Test
    @DisplayName("Programa Completo: Cálculo de Fatorial (iterativo)")
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
    @DisplayName("Programa Completo: Fibonacci")
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
    @DisplayName("Programa Completo: Verificar número par")
    void testNumeroPar() {
        String fonte = String.join("\n",
                "int num = 10;",
                "int resto = num % 2;",
                "if (resto == 0) {",
                "  print(1);", // 1 = par
                "} else {",
                "  print(0);", // 0 = ímpar
                "}"
        );
        assertDoesNotThrow(() -> compilarEExecutar(fonte));
    }
}
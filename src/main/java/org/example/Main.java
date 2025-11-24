package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.example.lexico.AnalisadorLexico;
import org.example.sintatico.Parser;
import org.example.sintatico.Stmt;
import org.example.sintatico.AstPrinter;
import org.example.semantico.AnalisadorSemantico;
import org.example.Interpretador;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                executarModoInterativo();
            } else {
                executarParaArquivo(args[0]);
            }
        } catch (Exception e) {
            System.err.println("[ERRO] Falha durante a análise: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void executarModoInterativo() throws IOException {
        System.out.println("==============================");
        System.out.println(" Modo interativo do mini-compilador");
        System.out.println("==============================");
        System.out.println("Digite seu código e finalize com uma linha contendo apenas 'EOF':");

        StringBuilder fonte = new StringBuilder();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) break;
                String linha = scanner.nextLine();
                if ("EOF".equalsIgnoreCase(linha.trim())) {
                    break;
                }
                fonte.append(linha).append(System.lineSeparator());
            }
        }

        if (fonte.length() == 0) {
            System.out.println("Nenhum código informado. Encerrando.");
            return;
        }

        processarFonte(fonte.toString(), "entrada interativa");
    }

    private static void executarParaArquivo(String caminhoArquivo) throws IOException {
        System.out.println("==============================");
        System.out.println(" Executando análise a partir de arquivo");
        System.out.println(" Arquivo: " + caminhoArquivo);
        System.out.println("==============================");

        String fonte = Files.readString(Path.of(caminhoArquivo));
        processarFonte(fonte, caminhoArquivo);
    }

    private static void processarFonte(String fonte, String origem) {
        System.out.println("[INFO] Iniciando lexing para " + origem + "...");
        AnalisadorLexico lexer = new AnalisadorLexico(fonte);
        List<AnalisadorLexico.Token> tokens = lexer.analisar();

        System.out.println("[INFO] Lexing concluído. Total de tokens: " + tokens.size());
        int idx = 0;
        for (AnalisadorLexico.Token token : tokens) {
            System.out.printf("[TOK %03d] %-12s %s%n", idx++, token.tipo, token.lexema);
        }

        Map<AnalisadorLexico.TipoToken, Integer> contagem = new EnumMap<>(AnalisadorLexico.TipoToken.class);
        for (AnalisadorLexico.Token t : tokens) {
            contagem.merge(t.tipo, 1, Integer::sum);
        }
        System.out.println("[INFO] Resumo por tipo de token:");
        contagem.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("   - %-15s : %d%n", e.getKey(), e.getValue()));

        System.out.println("[INFO] Iniciando parsing...");
        Parser parser = new Parser(tokens);
        List<Stmt> programa = parser.analisar();
        System.out.println("[INFO] Parsing concluído. Total de declarações: " + programa.size());

        AstPrinter printer = new AstPrinter();
        System.out.println("[INFO] AST gerada:");
        int stmtIdx = 1;
        for (Stmt s : programa) {
            System.out.println("   [" + stmtIdx++ + "] " + printer.print(s));
        }

        System.out.println("[INFO] Iniciando Análise Semântica...");
        AnalisadorSemantico semantico = new AnalisadorSemantico();
        semantico.analisar(programa);
        System.out.println("[INFO] Semântica OK!");

        System.out.println("[INFO] Iniciando execução do programa...");
        Interpretador interpretador = new Interpretador();
        interpretador.executar(programa);
        System.out.println("[INFO] Execução finalizada.");

        System.out.println("[INFO] Fluxo completo finalizado.");
    }
}
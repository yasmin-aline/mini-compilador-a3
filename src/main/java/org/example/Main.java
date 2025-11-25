package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;
import org.example.gerador.GeradorCodigo;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.example.lexico.AnalisadorLexico;
import org.example.semantico.AnalisadorSemantico;
import org.example.sintatico.AstPrinter;
import org.example.sintatico.Parser;
import org.example.sintatico.Stmt;
import org.example.icg.GeradorDeCodigo;
import org.example.icg.InterpretadorIC;
import org.example.icg.Instrucao;

public class Main {
    private static final String DIRETORIO_PADRAO = "src/test/resources/";
    private static final String[] EXTENSOES_ACEITAS = {".txt", ".kl"};
    
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                executarParaArquivo(args[0]);
            } else {
                encontrarEExecutarArquivo();
            }
        } catch (Exception e) {
            System.err.println("[ERRO] Falha durante a execução: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void encontrarEExecutarArquivo() throws IOException {
        File diretorio = new File(DIRETORIO_PADRAO);
        
        if (!diretorio.exists() || !diretorio.isDirectory()) {
            System.out.println("[INFO] Diretório não encontrado: " + diretorio.getAbsolutePath());
            executarModoInterativo();
            return;
        }

        Optional<File> arquivoEncontrado = buscarPrimeiroArquivoValido(diretorio);
        
        if (arquivoEncontrado.isPresent()) {
            File arquivo = arquivoEncontrado.get();
            System.out.println("[INFO] Arquivo encontrado: " + arquivo.getPath());
            executarParaArquivo(arquivo.getPath());
        } else {
            System.out.println("[INFO] Nenhum arquivo " + String.join(" ou ", EXTENSOES_ACEITAS) + 
                             " encontrado em " + diretorio.getAbsolutePath());
            executarModoInterativo();
        }
    }

    private static Optional<File> buscarPrimeiroArquivoValido(File diretorio) {
        Predicate<String> filtroExtensoes = nome -> {
            for (String ext : EXTENSOES_ACEITAS) {
                if (nome.endsWith(ext)) return true;
            }
            return false;
        };
        
        File[] arquivos = diretorio.listFiles((dir, nome) -> filtroExtensoes.test(nome));
        return arquivos != null && arquivos.length > 0 ? Optional.of(arquivos[0]) : Optional.empty();
    }

    private static void executarModoInterativo() throws IOException {
        exibirCabecalho("Modo interativo do mini-compilador");
        System.out.println("Digite seu código e finalize com uma linha contendo apenas 'EOF':");

        String codigoFonte = lerEntradaDoUsuario();
        if (codigoFonte.isEmpty()) {
            System.out.println("Nenhum código informado. Encerrando.");
            return;
        }

        processarFonte(codigoFonte, "entrada interativa");
    }

    private static String lerEntradaDoUsuario() throws IOException {
        StringBuilder fonte = new StringBuilder();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) break;
                
                String linha = scanner.nextLine().trim();
                if (linha.equalsIgnoreCase("EOF")) {
                    break;
                }
                fonte.append(linha).append(System.lineSeparator());
            }
        }
        return fonte.toString().trim();
    }

    private static void executarParaArquivo(String caminhoArquivo) throws IOException {
        exibirCabecalho("Executando análise a partir de arquivo");
        System.out.println("Arquivo: " + caminhoArquivo); 
        
        String conteudo = Files.readString(Path.of(caminhoArquivo));
        processarFonte(conteudo, caminhoArquivo);
    }
    
    private static void exibirCabecalho(String titulo) {
        System.out.println("\n" + "=".repeat(30));
        System.out.println(" " + titulo);
        System.out.println("=".repeat(30));
    }

    private static void processarFonte(String fonte, String origem) {
        List<AnalisadorLexico.Token> tokens = executarAnaliseLexica(fonte, origem);
        List<Stmt> programa = executarAnaliseSintatica(tokens);
        executarAnaliseSemantica(programa);
        executarTranspilacaoJava(programa); 
    }



    private static List<AnalisadorLexico.Token> executarAnaliseLexica(String fonte, String origem) {
        System.out.println("[INFO] Iniciando lexing para " + origem + "...");
        AnalisadorLexico lexer = new AnalisadorLexico(fonte);
        List<AnalisadorLexico.Token> tokens = lexer.analisar();

        exibirTokens(tokens);
        exibirResumoTokens(tokens);
        
        return tokens;
    }

    private static void exibirTokens(List<AnalisadorLexico.Token> tokens) {
        System.out.println("[INFO] Lexing concluído. Total de tokens: " + tokens.size());
        int idx = 0;
        for (AnalisadorLexico.Token token : tokens) {
            System.out.printf("[TOK %03d] %-12s %s%n", idx++, token.tipo, token.lexema);
        }
    }

    private static void exibirResumoTokens(List<AnalisadorLexico.Token> tokens) {
        Map<AnalisadorLexico.TipoToken, Integer> contagem = new EnumMap<>(AnalisadorLexico.TipoToken.class);
        for (AnalisadorLexico.Token t : tokens) {
            contagem.merge(t.tipo, 1, Integer::sum);
        }
        
        System.out.println("[INFO] Resumo por tipo de token:");
        contagem.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("   - %-15s : %d%n", e.getKey(), e.getValue()));
    }

    private static List<Stmt> executarAnaliseSintatica(List<AnalisadorLexico.Token> tokens) {
        System.out.println("[INFO] Iniciando parsing...");
        Parser parser = new Parser(tokens);
        List<Stmt> programa = parser.analisar();
        
        System.out.println("[INFO] Parsing concluído. Total de declarações: " + programa.size());
        exibirArvoreSintatica(programa);
        
        return programa;
    }

    private static void exibirArvoreSintatica(List<Stmt> programa) {
        AstPrinter printer = new AstPrinter();
        System.out.println("[INFO] AST gerada:");
        int stmtIdx = 1;
        for (Stmt s : programa) {
            System.out.println("   [" + stmtIdx++ + "] " + printer.print(s));
        }
    }

    private static void executarAnaliseSemantica(List<Stmt> programa) {
        System.out.println("[INFO] Iniciando Análise Semântica...");
        AnalisadorSemantico semantico = new AnalisadorSemantico();
        semantico.analisar(programa);
        System.out.println("[INFO] Semântica OK!");
    }

    private static List<Instrucao> executarGeracaoDeCodigo(List<Stmt> programa) {
        System.out.println("[INFO] Iniciando Geração de Código Intermediário...");
        GeradorDeCodigo gerador = new GeradorDeCodigo();
        List<Instrucao> codigo = gerador.gerar(programa);

        System.out.println("[INFO] Código Intermediário Gerado:");
        int idx = 0;
        for (Instrucao inst : codigo) {
            System.out.printf("[IC %03d] %s%n", idx++, inst);
        }
        System.out.println("[INFO] Geração de Código concluída.");
        return codigo;
    }

    private static void executarCodigoIntermediario(List<Instrucao> codigo) {
        System.out.println("[INFO] Iniciando execução do Código Intermediário...");
        try {
            InterpretadorIC interpretador = new InterpretadorIC(codigo);
            interpretador.executar();
            System.out.println("[INFO] Execução do Código Intermediário finalizada com sucesso.");
        } catch (Exception e) {
            System.err.println("[ERRO] Falha durante a execução do IC: " + e.getMessage());
            throw e;
        } finally {
            System.out.println("[INFO] Fluxo completo finalizado.");
        }
    }
    private static void executarTranspilacaoJava(List<Stmt> programa) {
        System.out.println("[INFO] Iniciando Transpilação para Java...");
        
        org.example.gerador.GeradorCodigo gerador = new org.example.gerador.GeradorCodigo();
        String codigoJava = gerador.gerarClasseJava(programa);

        System.out.println("\n--- CÓDIGO JAVA GERADO ---\n");
        System.out.println(codigoJava);
        System.out.println("--------------------------\n");

        try {
            Path caminhoArquivo = Path.of("ProgramaCompilado.java");
            Files.writeString(caminhoArquivo, codigoJava);
            System.out.println("[SUCESSO] Arquivo 'ProgramaCompilado.java' salvo.");

            System.out.println("[INFO] Compilando automaticamente...");
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            
            if (compiler == null) {
                System.err.println("[ERRO] Não foi possível encontrar o compilador Java (javac).");
                System.err.println("Certifique-se de estar rodando com um JDK, não apenas um JRE.");
                return;
            }

            int resultadoCompilacao = compiler.run(null, null, null, caminhoArquivo.toString());

            if (resultadoCompilacao == 0) {
                System.out.println("[SUCESSO] Compilação concluída! Arquivo 'ProgramaCompilado.class' gerado.");
                
                System.out.println("[INFO] Executando o programa agora...\n");
                System.out.println(">>> SAÍDA DO SEU PROGRAMA: <<<\n");
                
                ProcessBuilder builder = new ProcessBuilder("java", "ProgramaCompilado");
                builder.redirectErrorStream(true);
                Process processo = builder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(processo.getInputStream()));
                String linha;
                while ((linha = reader.readLine()) != null) {
                    System.out.println(linha);
                }
                
                System.out.println("\n>>> FIM DA EXECUÇÃO <<<");
                
            } else {
                System.err.println("[ERRO] Falha na compilação do Java gerado.");
            }

        } catch (IOException e) {
            System.err.println("[ERRO] Falha de I/O: " + e.getMessage());
        }
    }
    // interpretador que executa direto pela AST - nao utilizado atualmente
    private static void executarPrograma(List<Stmt> programa) {
        System.out.println("[INFO] Iniciando execução do programa...");
        try {
            Interpretador interpretador = new Interpretador();
            interpretador.executar(programa);
            System.out.println("[INFO] Execução finalizada com sucesso.");
        } catch (Exception e) {
            System.err.println("[ERRO] Falha durante a execução: " + e.getMessage());
            throw e;
        } finally {
            System.out.println("[INFO] Fluxo completo finalizado.");
        }
    }
}
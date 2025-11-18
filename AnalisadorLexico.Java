import java.util.ArrayList;
import java.util.List;

public class AnalisadorLexico {

    // primeira parte, vai definir a dipagem dos tokens 
    public enum TipoToken {
        PC_INT, PC_REAL, PC_STRING, // Palavras chave tipos
        PC_IF, PC_ELSE, PC_WHILE, PC_FUNC, PC_MAIN, // Controle
        PC_READ, PC_PRINT, // Entrada/Saída
        
        ID,             // Variáveis e nomes
        NUM_INT,        // 10
        NUM_REAL,       // 2.5
        TEXTO_STRING,   // "Oi"
        
        OP_ATRIB,       // =
        OP_ARIT,        // +, -, *, /
        OP_REL,         // >, <, ==, !=
        OP_LOGICO,      // &&, ||
        
        DELIM,          // ; ( ) { }
        EOF             // Fim 
    }

    // segunda parte a estrutura do token
    public static class Token {
        public TipoToken tipo;
        public String lexema; // texto original (ex: "x", "10", "if")

        public Token(TipoToken tipo, String lexema) {
            this.tipo = tipo;
            this.lexema = lexema;
        }

        @Override
        public String toString() {
            // vai fazer formatação para ficar mais "bonitinho" no terminal: [TIPO, valor]
            return String.format("[%s, \"%s\"]", tipo, lexema);
        }
    }

    // terceira parte,é a logica do analisador (scanner)
    private String codigo;
    private int pos = 0;

    public AnalisadorLexico(String codigo) {
        this.codigo = codigo;
    }

    public List<Token> analisar() {
        List<Token> tokens = new ArrayList<>();
        
        while (pos < codigo.length()) {
            char atual = codigo.charAt(pos);

            // ignora espaços, tabs e novas linhas
            if (Character.isWhitespace(atual)) {
                pos++;
                continue;
            }

            // identificação de Números (Inteiros e Reais)
            if (Character.isDigit(atual)) {
                tokens.add(lerNumero());
            }
            // identificação Palavras-chave ou Identificadores
            else if (Character.isLetter(atual)) {
                tokens.add(lerPalavra());
            }
            // identificação Strings ("texto")
            else if (atual == '"') {
                tokens.add(lerString());
            }
            // Símbolos e Operadores
            else {
                tokens.add(lerSimbolo(atual));
            }
        }
        tokens.add(new Token(TipoToken.EOF, ""));
        return tokens;
    }

    private Token lerNumero() {
        int inicio = pos;
        boolean ehReal = false;
        
        while (pos < codigo.length() && (Character.isDigit(codigo.charAt(pos)) || codigo.charAt(pos) == '.')) {
            if (codigo.charAt(pos) == '.') {
                if (ehReal) break; // se já tem um ponto, para.
                ehReal = true;
            }
            pos++;
        }
        String valor = codigo.substring(inicio, pos);
        return new Token(ehReal ? TipoToken.NUM_REAL : TipoToken.NUM_INT, valor);
    }

    private Token lerString() {
        pos++; // Pula a aspa inicial
        int inicio = pos;
        while (pos < codigo.length() && codigo.charAt(pos) != '"') {
            pos++;
        }
        String valor = codigo.substring(inicio, pos);
        pos++; // Pula a aspa final
        return new Token(TipoToken.TEXTO_STRING, valor);
    }

    private Token lerPalavra() {
        int inicio = pos;
        while (pos < codigo.length() && (Character.isLetterOrDigit(codigo.charAt(pos)) || codigo.charAt(pos) == '_')) {
            pos++;
        }
        String palavra = codigo.substring(inicio, pos);

        // Verifica se é palavra reservada
        switch (palavra) {
            case "int": return new Token(TipoToken.PC_INT, palavra);
            case "real": return new Token(TipoToken.PC_REAL, palavra);
            case "string": return new Token(TipoToken.PC_STRING, palavra);
            case "if": return new Token(TipoToken.PC_IF, palavra);
            case "else": return new Token(TipoToken.PC_ELSE, palavra);
            case "while": return new Token(TipoToken.PC_WHILE, palavra);
            case "read": return new Token(TipoToken.PC_READ, palavra);
            case "print": return new Token(TipoToken.PC_PRINT, palavra);
            case "func": return new Token(TipoToken.PC_FUNC, palavra);
            case "main": return new Token(TipoToken.PC_MAIN, palavra);
            default: return new Token(TipoToken.ID, palavra);
        }
    }

    private Token lerSimbolo(char c) {
        char proximo = (pos + 1 < codigo.length()) ? codigo.charAt(pos + 1) : '\0';
        
        // Operadores com 2 caracteres (==, !=, &&, ||)
        if (c == '=' && proximo == '=') { pos += 2; return new Token(TipoToken.OP_REL, "=="); }
        if (c == '!' && proximo == '=') { pos += 2; return new Token(TipoToken.OP_REL, "!="); }
        if (c == '&' && proximo == '&') { pos += 2; return new Token(TipoToken.OP_LOGICO, "&&"); }
        if (c == '|' && proximo == '|') { pos += 2; return new Token(TipoToken.OP_LOGICO, "||"); }
        
        pos++; // Avança 1 caractere para os casos abaixo

        switch (c) {
            case '=': return new Token(TipoToken.OP_ATRIB, "=");
            case '+': case '-': case '*': case '/': return new Token(TipoToken.OP_ARIT, String.valueOf(c));
            case '>': case '<': return new Token(TipoToken.OP_REL, String.valueOf(c));
            case '(': case ')': case '{': case '}': case ';': return new Token(TipoToken.DELIM, String.valueOf(c));
            default: throw new RuntimeException("Erro Léxico: Caractere inválido '" + c + "'");
        }
    }

    // quarta parte, metodo principal (pra rodar o teste)
    public static void main(String[] args) {
        String codigoExemplo = 
            "int x = 10;\n" +
            "real y = 2.5;\n" +
            "if (x > 5) {\n" +
            "    print(\"Maior\");\n" +
            "}";

        System.out.println(">>> Iniciando Análise Léxica...");
        AnalisadorLexico lexer = new AnalisadorLexico(codigoExemplo);
        
        try {
            List<Token> resultado = lexer.analisar();
            for (Token t : resultado) {
                System.out.println(t);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        System.out.println(">>> Análise Finalizada.");
    }
}
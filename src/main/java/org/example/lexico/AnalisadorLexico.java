package org.example.lexico;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AnalisadorLexico {

    public enum TipoToken {
        PC_INT, PC_REAL, PC_STRING,
        PC_IF, PC_ELSE, PC_WHILE, PC_FUNC, PC_MAIN,
        PC_READ, PC_PRINT,
        ID,
        NUM_INT,
        NUM_REAL,
        TEXTO_STRING,
        OP_ATRIB,
        OP_ARIT,
        OP_REL,
        OP_LOGICO,
        DELIM,
        EOF
    }

    public static class Token {
        public TipoToken tipo;
        public String lexema;

        public Token(TipoToken tipo, String lexema) {
            this.tipo = tipo;
            this.lexema = lexema;
        }

        @Override
        public String toString() {
            return String.format("[%s, \"%s\"]", tipo, lexema);
        }
    }

    private String codigo;
    private int pos = 0;

    public AnalisadorLexico(String codigo) {
        this.codigo = codigo;
    }

    public List<Token> analisar() {
        List<Token> tokens = new ArrayList<>();

        while (pos < codigo.length()) {
            char atual = codigo.charAt(pos);

            if (Character.isWhitespace(atual)) { pos++; continue; }

            if (Character.isDigit(atual)) {
                tokens.add(lerNumero());
            } else if (Character.isLetter(atual)) {
                tokens.add(lerPalavra());
            } else if (atual == '"') {
                tokens.add(lerString());
            } else {
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
                if (ehReal) break;
                ehReal = true;
            }
            pos++;
        }
        String valor = codigo.substring(inicio, pos);
        return new Token(ehReal ? TipoToken.NUM_REAL : TipoToken.NUM_INT, valor);
    }

    private Token lerString() {
        pos++;
        int inicio = pos;
        while (pos < codigo.length() && codigo.charAt(pos) != '"') {
            pos++;
        }
        String valor = codigo.substring(inicio, pos);
        if (pos < codigo.length()) pos++;
        return new Token(TipoToken.TEXTO_STRING, valor);
    }

    private Token lerPalavra() {
        int inicio = pos;
        while (pos < codigo.length() && (Character.isLetterOrDigit(codigo.charAt(pos)) || codigo.charAt(pos) == '_')) {
            pos++;
        }
        String palavra = codigo.substring(inicio, pos);

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

        if (c == '=' && proximo == '=') { pos += 2; return new Token(TipoToken.OP_REL, "=="); }
        if (c == '!' && proximo == '=') { pos += 2; return new Token(TipoToken.OP_REL, "!="); }
        if (c == '&' && proximo == '&') { pos += 2; return new Token(TipoToken.OP_LOGICO, "&&"); }
        if (c == '|' && proximo == '|') { pos += 2; return new Token(TipoToken.OP_LOGICO, "||"); }
        if (c == '>' && proximo == '=') { pos += 2; return new Token(TipoToken.OP_REL, ">="); }
        if (c == '<' && proximo == '=') { pos += 2; return new Token(TipoToken.OP_REL, "<="); }

        pos++;
        switch (c) {
            case '=': return new Token(TipoToken.OP_ATRIB, "=");
            case '+': case '-': case '*': case '/': case '%': return new Token(TipoToken.OP_ARIT, String.valueOf(c));
            case '>': case '<': return new Token(TipoToken.OP_REL, String.valueOf(c));
            case '(': case ')': case '{': case '}': case ';': return new Token(TipoToken.DELIM, String.valueOf(c));
            default: throw new RuntimeException("Erro Léxico: Caractere inválido '" + c + "'");
        }
    }

    public static List<Token> analisarArquivo(String caminhoArquivo) {
        try {
            String conteudo = new String(Files.readAllBytes(Paths.get(caminhoArquivo)));
            return new AnalisadorLexico(conteudo).analisar();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler o arquivo: " + e.getMessage(), e);
        }
    }
}

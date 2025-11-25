package org.example.sintatico;

import org.example.lexico.AnalisadorLexico.Token;
import org.example.lexico.AnalisadorLexico.TipoToken;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int posicaoAtual = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> analisar() {
        List<Stmt> declaracoes = new ArrayList<>();
        while (!fimDoArquivo()) {
            declaracoes.add(declaracao());
        }
        return declaracoes;
    }

    private Stmt declaracao() {
        if (verificar(TipoToken.PC_INT) || verificar(TipoToken.PC_REAL) || 
            verificar(TipoToken.PC_STRING)) {
            return declaracaoVariavel();
        }
        return comando();
    }

    private Stmt declaracaoVariavel() {
        String tipo = consumir().lexema;
        Token nome = consumir(TipoToken.ID, "Esperado nome de variável");
        
        Expr inicializacao = null;
        if (verificar(TipoToken.OP_ATRIB)) {
            consumir();
            inicializacao = expressao();
        }
        
        consumirDelimitador(";");
        return new Stmt.Var(tipo, nome.lexema, inicializacao);
    }

    private Stmt comando() {
        if (verificar(TipoToken.PC_PRINT)) return comandoPrint();
        if (verificar(TipoToken.PC_READ)) return comandoRead();
        if (verificar(TipoToken.PC_IF)) return comandoIf();
        if (verificar(TipoToken.PC_WHILE)) return comandoWhile();
        if (verificarDelimitador("{")) return bloco();
        return comandoExpressao();
    }

    private Stmt comandoPrint() {
        consumir(TipoToken.PC_PRINT, "Esperado 'print'");
        consumirDelimitador("(");
        Expr valor = expressao();
        consumirDelimitador(")");
        consumirDelimitador(";");
        return new Stmt.Print(valor);
    }

    private Stmt comandoRead() {
        consumir(TipoToken.PC_READ, "Esperado 'read'");
        consumirDelimitador("(");
        Token nome = consumir(TipoToken.ID, "Esperado nome de variável em read");
        consumirDelimitador(")");
        consumirDelimitador(";");
        return new Stmt.Read(nome.lexema);
    }

    private Stmt comandoIf() {
        consumir(TipoToken.PC_IF, "Esperado 'if'");
        consumirDelimitador("(");
        Expr condicao = expressao();
        consumirDelimitador(")");
        
        Stmt ramoEntao = comando();
        Stmt ramoSenao = null;
        
        if (verificar(TipoToken.PC_ELSE)) {
            consumir();
            ramoSenao = comando();
        }
        
        return new Stmt.If(condicao, ramoEntao, ramoSenao);
    }

    private Stmt comandoWhile() {
        consumir(TipoToken.PC_WHILE, "Esperado 'while'");
        consumirDelimitador("(");
        Expr condicao = expressao();
        consumirDelimitador(")");
        Stmt corpo = comando();
        return new Stmt.While(condicao, corpo);
    }

    private Stmt.Block bloco() {
        consumirDelimitador("{");
        List<Stmt> comandos = new ArrayList<>();
        while (!verificarDelimitador("}") && !fimDoArquivo()) {
            comandos.add(declaracao());
        }
        consumirDelimitador("}");
        return new Stmt.Block(comandos);
    }

    private Stmt comandoExpressao() {
        Expr expr = expressao();
        consumirDelimitador(";");
        return new Stmt.ExprStmt(expr);
    }

    private Expr expressao() {
        return atribuicao();
    }

    private Expr atribuicao() {
        Expr expr = ouLogico();
        
        if (verificar(TipoToken.OP_ATRIB)) {
            Token igual = consumir();
            Expr valor = atribuicao();
            
            if (expr instanceof Expr.Variable) {
                return new Expr.Binary(expr, igual.lexema, valor);
            }
            throw new RuntimeException("Alvo de atribuição inválido");
        }
        
        return expr;
    }

    private Expr ouLogico() {
        Expr expr = eLogico();
        
        while (verificar(TipoToken.OP_LOGICO) && verificarLexemaAtual("||")) {
            Token operador = consumir();
            Expr direita = eLogico();
            expr = new Expr.Logical(expr, operador.lexema, direita);
        }
        
        return expr;
    }

    private Expr eLogico() {
        Expr expr = igualdade();
        
        while (verificar(TipoToken.OP_LOGICO) && verificarLexemaAtual("&&")) {
            Token operador = consumir();
            Expr direita = igualdade();
            expr = new Expr.Logical(expr, operador.lexema, direita);
        }
        
        return expr;
    }

    private Expr igualdade() {
        Expr expr = comparacao();
        
        while (verificar(TipoToken.OP_REL) && 
               (verificarLexemaAtual("==") || verificarLexemaAtual("!="))) {
            Token operador = consumir();
            Expr direita = comparacao();
            expr = new Expr.Binary(expr, operador.lexema, direita);
        }
        
        return expr;
    }

    private Expr comparacao() {
        Expr expr = termo();
        
        while (verificar(TipoToken.OP_REL) && 
               (verificarLexemaAtual(">") || verificarLexemaAtual("<") || 
                verificarLexemaAtual(">=") || verificarLexemaAtual("<="))) {
            Token operador = consumir();
            Expr direita = termo();
            expr = new Expr.Binary(expr, operador.lexema, direita);
        }
        
        return expr;
    }

    private Expr termo() {
        Expr expr = fator();
        
        while (verificar(TipoToken.OP_ARIT) && 
               (verificarLexemaAtual("+") || verificarLexemaAtual("-"))) {
            Token operador = consumir();
            Expr direita = fator();
            expr = new Expr.Binary(expr, operador.lexema, direita);
        }
        
        return expr;
    }

    private Expr fator() {
        Expr expr = unario();
        
        while (verificar(TipoToken.OP_ARIT) && 
               (verificarLexemaAtual("*") || verificarLexemaAtual("/") || 
                verificarLexemaAtual("%"))) {
            Token operador = consumir();
            Expr direita = unario();
            expr = new Expr.Binary(expr, operador.lexema, direita);
        }
        
        return expr;
    }

    private Expr unario() {
        if (verificar(TipoToken.OP_ARIT) && verificarLexemaAtual("-")) {
            Token operador = consumir();
            Expr direita = unario();
            return new Expr.Unary(operador.lexema, direita);
        }
        return primario();
    }

private Expr primario() {
        if (verificar(TipoToken.NUM_INT)) {
            String texto = consumir().lexema;
            return new Expr.Literal(Integer.parseInt(texto));
        }

        if (verificar(TipoToken.NUM_REAL)) {
            String texto = consumir().lexema;
            return new Expr.Literal(Double.parseDouble(texto));
        }

        if (verificar(TipoToken.TEXTO_STRING)) {
            return new Expr.Literal(consumir().lexema);
        }
        
        if (verificar(TipoToken.ID)) {
            return new Expr.Variable(consumir().lexema);
        }
        
        if (verificarDelimitador("(")) {
            consumirDelimitador("(");
            Expr expr = expressao();
            consumirDelimitador(")");
            return new Expr.Grouping(expr);
        }
        
        throw new RuntimeException("Expressão inesperada: " + atual().tipo);
    }

    private Token consumir(TipoToken tipo, String mensagemErro) {
        if (verificar(tipo)) return consumir();
        throw new RuntimeException(mensagemErro);
    }

    private void consumirDelimitador(String delimitador) {
        if (verificarDelimitador(delimitador)) {
            consumir();
            return;
        }
        throw new RuntimeException("Esperado delimitador: " + delimitador);
    }

    private boolean verificar(TipoToken tipo) {
        if (fimDoArquivo()) return false;
        return atual().tipo == tipo;
    }

    private boolean verificarDelimitador(String valor) {
        if (fimDoArquivo()) return false;
        return atual().tipo == TipoToken.DELIM && atual().lexema.equals(valor);
    }

    private boolean verificarLexemaAtual(String valor) {
        if (fimDoArquivo()) return false;
        return atual().lexema.equals(valor);
    }

    private Token consumir() {
        if (!fimDoArquivo()) posicaoAtual++;
        return anterior();
    }

    private boolean fimDoArquivo() {
        return atual().tipo == TipoToken.EOF;
    }

    private Token atual() {
        return tokens.get(posicaoAtual);
    }

    private Token anterior() {
        return tokens.get(posicaoAtual - 1);
    }
}
package org.example.sintatico;

import org.example.lexico.AnalisadorLexico.Token;
import org.example.lexico.AnalisadorLexico.TipoToken;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parsePrograma() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaracao());
        }
        return statements;
    }

    private Stmt declaracao() {
        if (match(TipoToken.PC_INT, TipoToken.PC_REAL, TipoToken.PC_STRING)) {
            String tipo = previous().lexema;
            return varDecl(tipo);
        }
        return statement();
    }

    private Stmt varDecl(String tipo) {
        String name = consume(TipoToken.ID, "Esperado nome de variável após tipo.").lexema;
        Expr initializer = null;
        if (match(TipoToken.OP_ATRIB)) {
            initializer = expression();
        }
        consumeDelim(";");
        return new Stmt.Var(tipo, name, initializer);
    }

    private Stmt statement() {
        if (match(TipoToken.PC_PRINT)) return printStmt();
        if (match(TipoToken.PC_IF)) return ifStmt();
        if (match(TipoToken.PC_WHILE)) return whileStmt();
        if (checkDelim("{")) return block();
        return exprStmt();
    }

    private Stmt printStmt() {
        consumeDelim("(");
        Expr value = expression();
        consumeDelim(")");
        consumeDelim(";");
        return new Stmt.Print(value);
    }

    private Stmt ifStmt() {
        consumeDelim("(");
        Expr cond = expression();
        consumeDelim(")");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TipoToken.PC_ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(cond, thenBranch, elseBranch);
    }

    private Stmt whileStmt() {
        consumeDelim("(");
        Expr cond = expression();
        consumeDelim(")");
        Stmt body = statement();
        return new Stmt.While(cond, body);
    }

    private Stmt.Block block() {
        consumeDelim("{");
        List<Stmt> statements = new ArrayList<>();
        while (!checkDelim("}") && !isAtEnd()) {
            statements.add(declaracao());
        }
        consumeDelim("}");
        return new Stmt.Block(statements);
    }

    private Stmt exprStmt() {
        Expr expr = expression();
        consumeDelim(";");
        return new Stmt.ExprStmt(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = logicOr();
        if (match(TipoToken.OP_ATRIB)) {
            if (expr instanceof Expr.Variable) {
                Expr value = assignment();
                return new Expr.Binary(expr, "=", value);
            } else {
                error("Atribuição à expressão não é permitida.");
            }
        }
        return expr;
    }

    private Expr logicOr() {
        Expr expr = logicAnd();
        while (matchOp(TipoToken.OP_LOGICO, "||")) {
            String op = previous().lexema;
            Expr right = logicAnd();
            expr = new Expr.Logical(expr, op, right);
        }
        return expr;
    }

    private Expr logicAnd() {
        Expr expr = equality();
        while (matchOp(TipoToken.OP_LOGICO, "&&")) {
            String op = previous().lexema;
            Expr right = equality();
            expr = new Expr.Logical(expr, op, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (matchOp(TipoToken.OP_REL, "==", "!=")) {
            String op = previous().lexema;
            Expr right = comparison();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (matchOp(TipoToken.OP_REL, ">", "<")) {
            String op = previous().lexema;
            Expr right = term();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (matchOp(TipoToken.OP_ARIT, "+", "-")) {
            String op = previous().lexema;
            Expr right = factor();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (matchOp(TipoToken.OP_ARIT, "*", "/", "%")) {
            String op = previous().lexema;
            Expr right = unary();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TipoToken.OP_ARIT) && previous().lexema.equals("-")) {
            return new Expr.Unary("-", unary());
        }
        return call();
    }

    private Expr call() {
        return primary();
    }

    private Expr primary() {
        if (match(TipoToken.NUM_INT, TipoToken.NUM_REAL)) {
            return new Expr.Literal(previous().lexema);
        }
        if (match(TipoToken.TEXTO_STRING)) {
            return new Expr.Literal(previous().lexema);
        }
        if (match(TipoToken.ID)) {
            return new Expr.Variable(previous().lexema);
        }
        if (checkDelim("(")) {
            consumeDelim("(");
            Expr expr = expression();
            consumeDelim(")");
            return new Expr.Grouping(expr);
        }
        error("Expressão primária inválida.");
        return null;
    }

    private boolean matchOp(TipoToken tipo, String... lexemas) {
        if (!check(tipo)) return false;
        String atual = peek().lexema;
        for (String lexema : lexemas) {
            if (lexema.equals(atual)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean match(TipoToken... types) {
        for (TipoToken t : types) {
            if (check(t)) { advance(); return true; }
        }
        return false;
    }

    private boolean check(TipoToken type) {
        if (isAtEnd()) return false;
        return peek().tipo == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().tipo == TipoToken.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private void error(String msg) {
        throw new RuntimeException("Erro sintático: " + msg);
    }

    private boolean matchDelim(String lexema) {
        if (!isAtEnd() && peek().tipo == TipoToken.DELIM && lexema.equals(peek().lexema)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean checkDelim(String lexema) {
        if (isAtEnd()) return false;
        return peek().tipo == TipoToken.DELIM && lexema.equals(peek().lexema);
    }

    private void consumeDelim(String lexema) {
        if (!matchDelim(lexema)) {
            error("Esperado delimitador '" + lexema + "'.");
        }
    }

    private Token consume(TipoToken type, String message) {
        if (check(type)) return advance();
        error(message);
        return null;
    }
}

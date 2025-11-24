package org.example;

import org.example.lexico.AnalisadorLexico;
import org.example.lexico.AnalisadorLexico.Token;
import org.example.sintatico.AstPrinter;
import org.example.sintatico.Expr;
import org.example.sintatico.Parser;
import org.example.sintatico.Stmt;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private List<Token> tokensFrom(String codigo) {
        return new AnalisadorLexico(codigo).analisar();
    }

    private List<Stmt> parse(String codigo) {
        Parser parser = new Parser(tokensFrom(codigo));
        return parser.analisar();
    }

    private String astSummary(String codigo) {
        AstPrinter printer = new AstPrinter();
        return parse(codigo).stream()
                .map(printer::print)
                .collect(Collectors.joining("\n"));
    }

    @Test
    void parseDeclaracoesEAtribuicoesSimples() {
        List<Stmt> stmts = parse("""
                int x = 10;
                x = x + 1;
                print(x);
                """);

        assertEquals(3, stmts.size());

        Stmt.Var varDecl = assertInstanceOf(Stmt.Var.class, stmts.get(0));
        assertEquals("int", varDecl.typeKeyword);
        assertEquals("x", varDecl.name);
        Expr.Literal init = assertInstanceOf(Expr.Literal.class, varDecl.initializer);
        assertEquals("10", init.value);

        Stmt.ExprStmt assignStmt = assertInstanceOf(Stmt.ExprStmt.class, stmts.get(1));
        Expr.Binary assignExpr = assertInstanceOf(Expr.Binary.class, assignStmt.expression);
        assertEquals("=", assignExpr.operator);
        assertInstanceOf(Expr.Variable.class, assignExpr.left);
        Expr.Binary addExpr = assertInstanceOf(Expr.Binary.class, assignExpr.right);
        assertEquals("+", addExpr.operator);
        assertInstanceOf(Expr.Variable.class, addExpr.left);
        Expr.Literal addLiteral = assertInstanceOf(Expr.Literal.class, addExpr.right);
        assertEquals("1", addLiteral.value);

        Stmt.Print printStmt = assertInstanceOf(Stmt.Print.class, stmts.get(2));
        assertInstanceOf(Expr.Variable.class, printStmt.expression);
    }

    @Test
    void parseControleFluxoComBlocosAninhados() {
        List<Stmt> stmts = parse("""
                int x = 0;
                while (x < 3) {
                    if (x == 1) {
                        print("um");
                    } else {
                        print(x);
                    }
                    x = x + 1;
                }
                """);

        assertEquals(2, stmts.size());
        assertInstanceOf(Stmt.Var.class, stmts.get(0));

        Stmt.While whileStmt = assertInstanceOf(Stmt.While.class, stmts.get(1));
        Expr.Binary cond = assertInstanceOf(Expr.Binary.class, whileStmt.condition);
        assertEquals("<", cond.operator);

        Stmt.Block body = assertInstanceOf(Stmt.Block.class, whileStmt.body);
        assertEquals(2, body.statements.size());

        Stmt.If ifStmt = assertInstanceOf(Stmt.If.class, body.statements.get(0));
        assertNotNull(ifStmt.elseBranch);
        Stmt.Block thenBlock = assertInstanceOf(Stmt.Block.class, ifStmt.thenBranch);
        assertEquals(1, thenBlock.statements.size());
        assertInstanceOf(Stmt.Print.class, thenBlock.statements.get(0));
        Stmt.Block elseBlock = assertInstanceOf(Stmt.Block.class, ifStmt.elseBranch);
        assertEquals(1, elseBlock.statements.size());
        assertInstanceOf(Stmt.Print.class, elseBlock.statements.get(0));

        Stmt.ExprStmt increment = assertInstanceOf(Stmt.ExprStmt.class, body.statements.get(1));
        Expr.Binary incrementExpr = assertInstanceOf(Expr.Binary.class, increment.expression);
        assertEquals("=", incrementExpr.operator);
    }

    @Test
    void respeitaPrecedenciaOperadores() {
        List<Stmt> stmts = parse("""
                x = 1 + 2 * 3 - 4 / 2;
                """);

        Stmt.ExprStmt stmt = assertInstanceOf(Stmt.ExprStmt.class, stmts.get(0));
        Expr.Binary assign = assertInstanceOf(Expr.Binary.class, stmt.expression);
        assertEquals("=", assign.operator);

        Expr.Binary minus = assertInstanceOf(Expr.Binary.class, assign.right);
        assertEquals("-", minus.operator);

        Expr.Binary plus = assertInstanceOf(Expr.Binary.class, minus.left);
        assertEquals("+", plus.operator);
        Expr.Literal literalOne = assertInstanceOf(Expr.Literal.class, plus.left);
        assertEquals("1", literalOne.value);
        Expr.Binary mult = assertInstanceOf(Expr.Binary.class, plus.right);
        assertEquals("*", mult.operator);

        Expr.Binary division = assertInstanceOf(Expr.Binary.class, minus.right);
        assertEquals("/", division.operator);
    }

    @Test
    void disparaErroQuandoFaltaPontoEVirgula() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> parse("""
                int x = 1
                print(x);
                """));
        assertTrue(ex.getMessage().contains("Esperado"));
    }

    @Test
    void geraAstTextualParaProgramaCompleto() {
        String resumo = astSummary("""
                int x = 10;
                real y = 2.5;
                print(x);
                """);

        assertEquals("""
                var(int x = 10)
                var(real y = 2.5)
                print(x)
                """.strip(), resumo);
    }
}

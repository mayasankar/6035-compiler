package edu.mit.compilers.trees;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStreamException;
import edu.mit.compilers.grammar.DecafParser;
import edu.mit.compilers.grammar.DecafScanner;
import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRProgram;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.IRIntLiteral;
import edu.mit.compilers.ir.statement.IRStatement;
import edu.mit.compilers.ir.statement.IRStatement.StatementType;
import edu.mit.compilers.symbol_tables.MethodTable;
import edu.mit.compilers.symbol_tables.VariableTable;;

public class ASTCreatorTest {
    private static final String EXPR = "expression";
    private static final String STAT = "statement";
    
    /* 
     * Tests for parseExpressionTree:
     * 1. location
     * 2. method call 
     * 3. literal
     *  a. char
     *  b. bool
     *  c. string
     *  d. int
     *   i. positive/negative
     *   ii. decimal/hex
     *  e. length;
     * 4. in parens
     * 5. not/neg
     * 6. multiply/divide
     * 7. add/substract
     * 8. relation
     * 9. equality
     * 10. logical and/or
     * 11. ternary op
     */
    @Test
    public void testInt() {
        verifyExprType("1234", IRExpression.ExpressionType.INT_LITERAL);
    }
    
    @Test
    public void testNegativeInt() {
        verifyExprType("(-1234)", IRExpression.ExpressionType.INT_LITERAL);
        verifyExprType("(-0)", IRExpression.ExpressionType.INT_LITERAL);
    }
    
    @Test
    public void testHexInts() {
        verifyExprType("0xDEADBEEF", IRExpression.ExpressionType.INT_LITERAL);
        verifyExprType("-0x12", IRExpression.ExpressionType.INT_LITERAL);
    }
    
    @Test
    public void testBools() {
        verifyExprType("true", IRExpression.ExpressionType.BOOL_LITERAL);
        verifyExprType("false", IRExpression.ExpressionType.BOOL_LITERAL);
    }
    
    @Test
    public void testChars() {
        verifyExprType("'a'", IRExpression.ExpressionType.INT_LITERAL);
        verifyExprType("'\\\\'", IRExpression.ExpressionType.INT_LITERAL);
    }
    
 /*   @Test
    public void testString() {
        verifyExprType("\"Hello world!\"", IRExpression.ExpressionType.STRING_LITERAL);
        verifyExprType("\"A \\\"lot\\\" \\n \\\\ \\t of escaped stuff \"", IRExpression.ExpressionType.STRING_LITERAL);
    }*/
    
    @Test
    public void testLocationParens() {
        verifyExprType("(foo)", IRExpression.ExpressionType.VARIABLE);
    }
    
    @Test
    public void testMethodCall() {
        IRExpression expr = verifyExprType("foo(1,true, a+3, \"string\")", IRExpression.ExpressionType.METHOD_CALL);
        IRMethodCallExpression method = (IRMethodCallExpression) expr;
        assertEquals("foo", method.getName());
        assertEquals(4, method.getArguments().size());
    }
    
    @Test
    public void testUnaryOpeartors() {
        verifyExprType("!true", IRExpression.ExpressionType.UNARY);
        verifyExprType("-(variable)", IRExpression.ExpressionType.UNARY);
    }
    
    @Test
    public void testMultiplication() {
        verifyExprType("a*(2+37/var)", IRExpression.ExpressionType.BINARY);
        verifyExprType("1/0", IRExpression.ExpressionType.BINARY);
    }
    
    @Test
    public void testAddition() {
        verifyExprType("1+2+3+4", IRExpression.ExpressionType.BINARY);
        verifyExprType("foo[20] - len(bar)", IRExpression.ExpressionType.BINARY);
    }
    
    @Test
    public void testRelationEquality() {
        verifyExprType("1<2", IRExpression.ExpressionType.BINARY);
        verifyExprType("1<=(var)", IRExpression.ExpressionType.BINARY);
        verifyExprType("-1==1", IRExpression.ExpressionType.BINARY);
        verifyExprType("true> false", IRExpression.ExpressionType.BINARY);
        verifyExprType("true != 1", IRExpression.ExpressionType.BINARY);
    }
    
    @Test
    public void testAndOr() {
        verifyExprType("a && b || false", IRExpression.ExpressionType.BINARY);
    }
    
    @Test
    public void testTernary() {
        verifyExprType("true ? 1:0", IRExpression.ExpressionType.TERNARY);
    }

    /* 
     * Tests for parseStatementTree:
     * 1. Assign expression
     * 2. method call
     * 3. if block
     * 4. for block
     * 5. while block
     * 6. return statement
     * 7. break statement
     * 8. continue statement
     */
    
    @Test
    public void testAssign() {
        verifyStatType("a=true?value:0;", StatementType.ASSIGN_EXPR);
    }
    
    @Test
    public void testMethod() {
        verifyStatType("print('h','e','l','l','o',' ','w','o','r','l','d');", StatementType.METHOD_CALL);
    }
    
    @Test
    public void testIfBlock() {
        verifyStatType("if(i<n){}", StatementType.IF_BLOCK);
        verifyStatType("if(i<n){}else{}", StatementType.IF_BLOCK);
    }
    
    @Test
    public void testForBlock() {
        verifyStatType("for(i=0;i<n;i++){}", StatementType.FOR_BLOCK);
    }
    
    @Test
    public void testWhileBlock() {
        verifyStatType("while(true){}", StatementType.WHILE_BLOCK);
    }
    
    @Test
    public void testToken() {
        Token tk = new CommonToken("=");
        
        assertEquals("=", tk.getText());
    }
    
    //TODO: break, continue, return statements
    private IRStatement verifyStatType(String exprStr, StatementType statType) {
        ConcreteTree tree = makeTreeFromString(exprStr, STAT);
        IRStatement stat = ASTCreator.parseStatement(tree, new VariableTable(), new MethodTable());
        assertEquals(statType, stat.getStatementType());
        assertNotNull(stat.getVariableTable());

        
        return stat;
    }
    
    private IRExpression verifyExprType(String exprStr, IRExpression.ExpressionType exprType) {
        ConcreteTree tree = makeTreeFromString(exprStr, EXPR);
        IRExpression expr = ASTCreator.parseExpressionTree(tree, new VariableTable(), new MethodTable());
        assertEquals(exprType, expr.getExpressionType());
        assertNotNull(expr.getVariableTable());
        
        return expr;
    }
    
    private ConcreteTree makeTreeFromString(String input, String typeOfNode) {
        InputStream stream = new ByteArrayInputStream(input.getBytes());
        DecafParser parser = new DecafParser(new DecafScanner(stream));
        try {
            switch(typeOfNode){
            case EXPR:
                parser.expr();
                break;
            case STAT:
                parser.statement();
                break;
            case "block":
                parser.block();
                break;
            default:
                parser.program();
            }
        } catch (Exception e) {
            System.out.println("The following input could not be parsed: " + input);
            fail();
        }
        ConcreteTree tree = parser.getParseTree();
        ASTCreator.simplifyTree(tree);
        return parser.getParseTree();
    }
}
package edu.mit.compilers.trees;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import edu.mit.compilers.grammar.DecafParser;
import edu.mit.compilers.grammar.DecafScanner;
import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRProgram;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.IRIntLiteral;;

public class ASTCreatorTest {
    private static final String EXPR = "expression";
    
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
        verifyExprType("1<=var", IRExpression.ExpressionType.BINARY);
        verifyExprType("-1==1", IRExpression.ExpressionType.BINARY);
        verifyExprType("true> false", IRExpression.ExpressionType.BINARY);
        verifyExprType("true != 1", IRExpression.ExpressionType.BINARY);
    }
    
    /* 
     * Tests for parseExpressionTree:
     * 8. relation
     * 9. equality
     * 10. logical and/or
     * 11. ternary op
     */
    private IRExpression verifyExprType(String exprStr, IRExpression.ExpressionType exprType) {
        ConcreteTree tree = makeTreeFromString(exprStr, EXPR);
        IRExpression expr = ASTCreator.parseExpressionTree(tree);
        assertEquals(exprType, expr.getExpressionType());
        
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
            case "statement":
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
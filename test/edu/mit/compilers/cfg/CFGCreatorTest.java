package edu.mit.compilers.cfg;

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
import edu.mit.compilers.symbol_tables.VariableTable;
import edu.mit.compilers.trees.ASTCreator;
import edu.mit.compilers.trees.ConcreteTree;;

public class CFGCreatorTest {
    private static final String EXPR = "expression";
    private static final String STAT = "statement";
    
    @Test
    public void testAssignCreator() {
        IRStatement stat = verifyStatType("a = b + c * d;", StatementType.ASSIGN_EXPR);
        CFGCreator creator = new CFGCreator();
        CFG cfg = creator.destructIRStatement(stat);
        System.out.println(cfg.toString());
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
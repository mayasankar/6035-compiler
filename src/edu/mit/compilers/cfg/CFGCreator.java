package edu.mit.compilers.cfg;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.math.BigInteger;

import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.operator.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;

// todo list
// multi-line, insert here

public class CFGCreator {

    public static CFGLine makeNoOp() {
        return new CFGNoOp();
    }

    public static CFG destruct(IRProgram ir) {
        // TODO
        throw new RuntimeException("Unimplemented");
    }

    private static CFG destructIRStatement(IRStatement statement){
        switch(statement.getStatementType()) {
          case IF_BLOCK: {
            return destructIRIfStatement((IRIfStatement) statement);
          } case FOR_BLOCK: {
            return destructIRForStatement((IRForStatement) statement);
          } case WHILE_BLOCK: {
            return destructIRWhileStatement((IRWhileStatement) statement);
          } case METHOD_CALL: case RETURN_EXPR: {
            // TODO idk how we're handling methods and returns?
          } case ASSIGN_EXPR: case BREAK: case CONTINUE: {
            return new CFG(new CFGStatement(statement));
          } default: {
            throw new RuntimeException("destructIR error: UNSPECIFIED statement");
          }
        }
    }

/*
    private static CFG destructIRExpression(IRExpression expr){
        switch(expr.getExpressionType()) {
          case BOOL_LITERAL: case STRING_LITERAL: {
            return;
        } case INT_LITERAL: {
            destructIntLiteral((IRIntLiteral) expr); break;
        } case UNARY: {
            destructIRUnaryOpExpression((IRUnaryOpExpression) expr); break;
          } case BINARY: {
            destructIRBinaryOpExpression((IRBinaryOpExpression) expr); break;
          } case TERNARY: {
            destructIRTernaryOpExpression((IRTernaryOpExpression) expr); break;
          } case LEN: {
            destructIRLenExpression((IRLenExpression) expr); break;
          } case METHOD_CALL: {
            destructIRMethodCallExpression((IRMethodCallExpression) expr); break;
          } case VARIABLE: {
            destructIRVariableExpression((IRVariableExpression) expr); break;
          } default: {
            notifyError("IR error: UNSPECIFIED expr", expr);
          }
        }
    }
*/ // uncertain if we need this

    private static CFG destructIRIfStatement(IRIfStatement statement) {
        IRExpression cond = statement.getCondition();
        IRBlock thenBlock = statement.getThenBlock();
        IRBlock elseBlock = statement.getElseBlock();
        CFG thenGraph = destructIRBlock(thenBlock);
        CFG elseGraph = destructIRBlock(elseBlock);
        CFGLine thenStart = thenGraph.getStart();
        CFGLine thenEnd = thenGraph.getEnd();
        CFGLine elseStart = elseGraph.getStart();
        CFGLine elseEnd = elseGraph.getEnd();
        CFGLine noOp = makeNoOp();
        thenEnd.setNext(noOp);
        elseEnd.setNext(noOp);
        CFGLine condStart = shortcircuit(cond, thenStart, elseStart);
        return new CFG(condStart, noOp);
    }
    private static CFG destructIRWhileStatement(IRWhileStatement statement) {
        IRExpression cond = statement.getCondition();
        IRBlock block = statement.getBlock();
        CFG blockGraph = destructIRBlock(block);
        CFGLine blockStart = blockGraph.getStart();
        CFGLine blockEnd = blockGraph.getEnd();
        CFGLine noOp = makeNoOp();
        CFGLine condStart = shortcircuit(cond, blockStart, noOp);
        blockEnd.setNext(condStart);
        return new CFG(condStart, noOp);
    }

    private static CFG destructIRForStatement(IRForStatement statement) {
        IRExpression cond = statement.getCondition();
        IRAssignStatement initializer = statement.getInitializer();
        IRAssignStatement stepFunction = statement.getStepFunction();
        IRBlock block = statement.getBlock();

        CFGStatement initLine = new CFGStatement(initializer);
        CFGStatement stepLine = new CFGStatement(stepFunction);
        CFG blockGraph = destructIRBlock(block);
        CFGLine blockStart = blockGraph.getStart();
        CFGLine blockEnd = blockGraph.getEnd();
        CFGLine noOp = makeNoOp();
        CFGLine condStart = shortcircuit(cond, blockStart, noOp);
        blockEnd.setNext(stepLine);
        stepLine.setNext(condStart);
        initLine.setNext(condStart);
        return new CFG(initLine, noOp);
    }

    private static CFG destructIRBlock(IRBlock block) {
        List<IRStatement> statements = block.getStatements();
        return destructStatementList(statements);
    }

    private static CFG destructStatementList(List<IRStatement> statements) {
        CFGStatement first = new CFGStatement(statements.remove(0));
        CFG restGraph = destructStatementList(statements);
        CFGLine restStart = restGraph.getStart();
        CFGLine restEnd = restGraph.getEnd();
        first.setNext(restStart);
        return new CFG(first, restEnd);
    }

    private static CFGLine shortcircuit(IRExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        throw new RuntimeException("Unimplemented");
    }

    private static CFGLine shortcircuitAndExpression(IRBinaryOpExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        CFGLine beginSecond = shortcircuit(expr.getRightExpr(), trueBranch, falseBranch);
        CFGLine beginFirst = shortcircuit(expr.getLeftExpr(), beginSecond, falseBranch);
        return beginFirst;
    }

    private static CFGLine shortcircuitOrExpression(IRBinaryOpExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        CFGLine beginSecond = shortcircuit(expr.getRightExpr(), trueBranch, falseBranch);
        CFGLine beginFirst = shortcircuit(expr.getLeftExpr(), trueBranch, beginSecond);
        return beginFirst;
    }

    private static CFGLine shortcircuitBasicExpression(IRBinaryOpExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        CFGLine begin = new CFGExpression(trueBranch, falseBranch, expr);
        return begin;
    }

    private static CFGLine shortcircuitNotExpression(IRUnaryOpExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        CFGLine beginNot = shortcircuit(expr.getArgument(), falseBranch, trueBranch);
        return beginNot;
    }

    // probably same here
}

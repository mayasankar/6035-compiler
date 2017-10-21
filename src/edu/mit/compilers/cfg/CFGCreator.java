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
// IRMethodCallExpression destructor

public class CFGCreator {

    public static CFGLine makeNoOp() {
        return new CFGNoOp();
    }

    public static Map<String, CFGBlock> destruct(IRProgram tree) {
        Map<String, CFGBlock> destructedMethods = new HashMap<>();
        for (IRMethodDecl method : tree.methods.getMethodList()) {
            CFG methodCFG = destructIRMethodDecl(method);
            CFGBlock blockedCFG = condenseIntoBlocks(methodCFG);
            String name = method.getName();
            destructedMethods.put(name, blockedCFG);
        }
        return destructedMethods;
    }

    private static CFGBlock condenseIntoBlocks(CFG cfg) {
        // make a new block
        // while we have lines that don't merge or branch, add them to block
        // then, for each child that isn't already part of a block, recursively run this
        CFGBlock block = new CFGBlock();
        CFGLine line = cfg.getStart();
        boolean broke_as_merge = false;
        while (line != null) {
            if (line.getCorrespondingBlock() != null) {
                throw new RuntimeException("This should never happen. line: " + line.ownValue() +
                ", Block: " + line.getCorrespondingBlock().toString());
            }
            if (!line.isNoOp()) {
                //System.out.println("line: " + line.ownValue());
                block.addLine(line);
            }
            line.setCorrespondingBlock(block);
            if (line.isBranch()) {
                break;
            }
            line = line.getTrueBranch();
            if (line == null || line.isMerge()) {
                broke_as_merge = true;
                break;
            }
        }
        if (line != null) {
            if (broke_as_merge) {
                if (line.getCorrespondingBlock() == null) {
                    CFG remainder = new CFG(line, cfg.getEnd());
                    CFGBlock condensedRemainder = condenseIntoBlocks(remainder);
                    block.setTrueBranch(condensedRemainder);
                    block.setFalseBranch(condensedRemainder);
                }
                else {
                    block.setTrueBranch(line.getCorrespondingBlock());
                    block.setFalseBranch(line.getCorrespondingBlock());
                }
            }
            else if (line.isBranch()) {
                CFGLine trueChild = line.getTrueBranch();
                if (trueChild.getCorrespondingBlock() == null) {
                    CFG remainder = new CFG(trueChild, cfg.getEnd());
                    CFGBlock condensedRemainder = condenseIntoBlocks(remainder);
                    block.setTrueBranch(condensedRemainder);
                }
                else {
                    block.setTrueBranch(trueChild.getCorrespondingBlock());
                }
                CFGLine falseChild = line.getFalseBranch();
                if (falseChild.getCorrespondingBlock() == null) {
                    CFG remainder = new CFG(falseChild, cfg.getEnd());
                    CFGBlock condensedRemainder = condenseIntoBlocks(remainder);
                    block.setFalseBranch(condensedRemainder);
                }
                else {
                    block.setFalseBranch(falseChild.getCorrespondingBlock());
                }
            }
            else {
                throw new RuntimeException("Impossible; if it broke it must be null, merge, or branch.");
            }
            if (block.getTrueBranch() == null || block.getFalseBranch() == null) {
                throw new RuntimeException("Block children should have been set to non-null value.");
            }
        }
        return block;
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
            // TODO idk how we're handling methods and returns? I think just as lines
          } case ASSIGN_EXPR: case BREAK: case CONTINUE: {
            return new CFG(new CFGStatement(statement));
          } default: {
            throw new RuntimeException("destructIR error: UNSPECIFIED statement");
          }
        }
    }

    private static CFG destructIRMethodDecl(IRMethodDecl decl) {
        // todo do something w/ MethodTable parameters?
        IRBlock code = decl.getCode();
        if (code == null) {
            // it's an import
            return new CFG(new CFGMethodDecl(decl));
        }
        return destructIRBlock(code);
    }

    private static CFG destructIRIfStatement(IRIfStatement statement) {
        // TODO handle nonexistence of else blocks
        IRExpression cond = statement.getCondition();
        IRBlock thenBlock = statement.getThenBlock();
        IRBlock elseBlock = statement.getElseBlock();

        CFG thenGraph = destructIRBlock(thenBlock);
        CFGLine thenStart = thenGraph.getStart();
        CFGLine thenEnd = thenGraph.getEnd();
        CFGLine noOp = makeNoOp();
        thenEnd.setNext(noOp);
        CFGLine condStart;
        if (elseBlock != null) {
            CFG elseGraph = destructIRBlock(elseBlock);
            CFGLine elseStart = elseGraph.getStart();
            CFGLine elseEnd = elseGraph.getEnd();
            elseEnd.setNext(noOp);
            condStart = shortcircuit(cond, thenStart, elseStart);
        }
        else {
            condStart = shortcircuit(cond, thenStart, noOp);
        }
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

    private static CFG destructIRTernaryOpExpression(IRTernaryOpExpression expr) {
        IRExpression condition = expr.getCondition();

        CFGLine trueBranch = destructIRExpression(expr.getTrueExpression()).getStart();
        CFGLine falseBranch = destructIRExpression(expr.getFalseExpression()).getStart();

        CFGLine condLine = shortcircuit(condition, trueBranch, falseBranch);
        CFGLine noOp = makeNoOp();
        return new CFG(condLine, noOp);
    }

    private static CFG destructIRExpression(IRExpression expr) {
        switch(expr.getExpressionType()) {
          case TERNARY: {
            return destructIRTernaryOpExpression((IRTernaryOpExpression) expr);
          }
          case METHOD_CALL: {
            return destructIRMethodCallExpression((IRMethodCallExpression) expr);
          }
          default: {
            return new CFG(new CFGExpression(expr));
          }
        }
    }

    private static CFG destructIRMethodCallExpression(IRMethodCallExpression expr) {
        // TODO
        throw new RuntimeException("Unimplemented");
    }

    private static CFG destructIRBlock(IRBlock block) {
        List<IRStatement> statements = block.getStatements();
        return destructStatementList(statements);
    }

    private static CFG destructStatementList(List<IRStatement> statements) {
        if (statements.size() == 0) {
            CFGLine noOp = makeNoOp();
            return new CFG(noOp);
        }
        CFG firstGraph = destructIRStatement(statements.remove(0));
        CFGLine firstEnd = firstGraph.getEnd();
        CFG restGraph = destructStatementList(statements);
        CFGLine restStart = restGraph.getStart();
        CFGLine restEnd = restGraph.getEnd();
        firstEnd.setNext(restStart);
        return new CFG(firstGraph.getStart(), restEnd);
    }

    private static CFGLine shortcircuit(IRExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        switch(expr.getExpressionType()) {
          case UNARY: {
            IRUnaryOpExpression unExpr = (IRUnaryOpExpression) expr;
            if (unExpr.getOperator().getText().equals("!")) {
                return shortcircuitNotExpression(unExpr, trueBranch, falseBranch);
            }
            return shortcircuitBasicExpression(expr, trueBranch, falseBranch);
          }
          case BINARY: {
            IRBinaryOpExpression biExpr = (IRBinaryOpExpression) expr;
            String op = biExpr.getOperator().getText();
            if (op.equals("&&")) {
                return shortcircuitAndExpression(biExpr, trueBranch, falseBranch);
            }
            if (op.equals("||")) {
                return shortcircuitOrExpression(biExpr, trueBranch, falseBranch);
            }
            return shortcircuitBasicExpression(expr, trueBranch, falseBranch);
          }
          default: {
            return shortcircuitBasicExpression(expr, trueBranch, falseBranch);
          }
        }
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

    private static CFGLine shortcircuitBasicExpression(IRExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        // todo do we ever need to destruct this?
        return new CFGExpression(trueBranch, falseBranch, expr);
    }

    private static CFGLine shortcircuitNotExpression(IRUnaryOpExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        CFGLine beginNot = shortcircuit(expr.getArgument(), falseBranch, trueBranch);
        return beginNot;
    }



}

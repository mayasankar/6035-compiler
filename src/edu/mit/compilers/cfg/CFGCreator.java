package edu.mit.compilers.cfg;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.math.BigInteger;

import antlr.CommonToken;
import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.IRExpression.ExpressionType;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;


public class CFGCreator {
    private static final Token EQ_OP = new CommonToken("=");

    List<CFGLoopEnv> envStack;

    public CFGCreator() {
        envStack = new ArrayList<>();
    }

    public CFGLine makeNoOp() {
        return new CFGNoOp();
    }

    public Map<String, CFGBlock> destruct(IRProgram tree) {
        Map<String, CFGBlock> destructedMethods = new HashMap<>();
        for (IRMethodDecl method : tree.getMethodTable().getMethodList()) {
            CFG methodCFG = destructIRMethodDecl(method);
            //methodCFG.deadCodeElimination();
            CFGBlock blockedCFG = condenseIntoBlocks(methodCFG);
            String name = method.getName();
            destructedMethods.put(name, blockedCFG);
        }
        return destructedMethods;
    }

    private CFGBlock condenseIntoBlocks(CFG cfg) {
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
            //System.out.println("line: " + line.ownValue());
            block.addLine(line);
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

    private CFG destructIRMemberDecl(IRMemberDecl decl) {
        return new CFG(new CFGDecl(decl));
    }

    public CFG destructIRStatement(IRStatement statement){
        switch(statement.getStatementType()) {
          case IF_BLOCK: {
            return destructIRIfStatement((IRIfStatement) statement);
          } case FOR_BLOCK: {
            return destructIRForStatement((IRForStatement) statement);
          } case WHILE_BLOCK: {
            return destructIRWhileStatement((IRWhileStatement) statement);
          } case METHOD_CALL: {
            return destructIRMethodCall((IRMethodCallStatement) statement);
          } case RETURN_EXPR: {
            return destructIRReturnStatement((IRReturnStatement) statement);
          } case ASSIGN_EXPR: {
            return destructIRAssignStatement((IRAssignStatement) statement);
          } case BREAK: {
              return destructBreakStatement();
          } case CONTINUE: {
              return destructContinueStatement();
          } default: {
            throw new RuntimeException("destructIR error: UNSPECIFIED statement");
          }
        }
    }

    private CFG destructIRMethodCall(IRMethodCallStatement statement) {
    	CFG answer = new CFG(makeNoOp());
    	int tempCounter = 0;
    	List<IRExpression> argsWithTemps = new ArrayList<>();
    	for(IRExpression arg: statement.getMethodCall().getArguments()) {
    		if(arg.getDepth() > 0) {
    			String tempVarName = "temp_" + statement.hashCode() + "_" + tempCounter;
    			tempCounter += 1;
    			CFG expandExpr = destructIRExpression(arg, tempVarName);
    			IRVariableExpression temp = new IRVariableExpression(tempVarName);
    			argsWithTemps.add(temp);
    			answer.concat(expandExpr);
    		} else {
    			argsWithTemps.add(arg);
    		}
    	}
    	IRMethodCallStatement newStat = new IRMethodCallStatement(new IRMethodCallExpression(statement.getMethodCall().getName(), argsWithTemps));
    	CFG newStatCFG = new CFG(new CFGStatement(newStat));
    	return answer.concat(newStatCFG);
	}

	private CFG destructIRReturnStatement(IRReturnStatement statement) {
        if (statement.isVoid()) {
            return new CFG(new CFGStatement(statement));
        }
        IRExpression returnExpr = statement.getReturnExpr();
        String name = "temp_return_" + statement.hashCode();
        CFG returnCFG = destructIRExpression(returnExpr, name);
        IRVariableExpression returnVar = new IRVariableExpression(name);
        CFG returnStat = new CFG(new CFGStatement(new IRReturnStatement(returnVar)));
        return returnCFG.concat(returnStat);
    }


    private CFG destructIRMethodDecl(IRMethodDecl decl) {
        if (decl.isImport()) {
            // TODO do we actually need to do anything with it?
            return new CFG(makeNoOp());
        }
        IRBlock code = decl.getCode();
        CFG methodDecl = new CFG(new CFGMethodDecl(decl));
        CFG graph = destructIRBlock(code);
        return methodDecl.concat(graph);
    }

    private CFG destructBreakStatement() {
        CFGLine noOp = makeNoOp();
        CFGLoopEnv containingLoop = envStack.get(envStack.size()-1);
        CFGLine followingLine = containingLoop.getFollowingLine();
        noOp.setNext(followingLine);
        return new CFG(noOp);
    }

    private CFG destructContinueStatement() {
        CFGLine noOp = makeNoOp();
        CFGLoopEnv containingLoop = envStack.get(envStack.size()-1);
        CFGLine startLine = containingLoop.getStartLine();
        noOp.setNext(startLine);
        return new CFG(noOp);
    }

    private CFG destructIRIfStatement(IRIfStatement statement) {
        IRExpression cond = statement.getCondition();
        IRBlock thenBlock = statement.getThenBlock();
        IRBlock elseBlock = statement.getElseBlock();

        List<CFGLoopEnv> envStackCopy = new ArrayList<>(envStack);

        CFG thenGraph = destructIRBlock(thenBlock);
        CFGLine thenStart = thenGraph.getStart();
        CFGLine thenEnd = thenGraph.getEnd();
        CFGLine noOp = makeNoOp();
        thenEnd.setNext(noOp);
        CFGLine condStart;
        if (elseBlock != null) {
            envStack = new ArrayList<>(envStackCopy); // restore, removing any changes we did while destructing child
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

    private CFG destructIRWhileStatement(IRWhileStatement statement) {
        CFGLine startNoOp = makeNoOp();
        CFGLine endNoOp = makeNoOp();
        envStack.add(new CFGLoopEnv(startNoOp, endNoOp));

        IRExpression cond = statement.getCondition();
        IRBlock block = statement.getBlock();
        CFG blockGraph = destructIRBlock(block);
        CFGLine blockStart = blockGraph.getStart();
        CFGLine blockEnd = blockGraph.getEnd();
        CFGLine condStart = shortcircuit(cond, blockStart, endNoOp);
        blockEnd.setNext(condStart);
        startNoOp.setNext(condStart);

        envStack.remove(envStack.size()-1);
        return new CFG(startNoOp, endNoOp);
    }

    private CFG destructIRForStatement(IRForStatement statement) {
        CFGLine continueNoOp = makeNoOp(); // when we continue, jump to here, which will go to the incrementor
        CFGLine endNoOp = makeNoOp();
        envStack.add(new CFGLoopEnv(continueNoOp, endNoOp));

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
        CFGLine condStart = shortcircuit(cond, blockStart, endNoOp);
        blockEnd.setNext(stepLine);
        stepLine.setNext(condStart);
        initLine.setNext(condStart);
        continueNoOp.setNext(stepLine);

        envStack.remove(envStack.size()-1);
        return new CFG(initLine, endNoOp);
    }

    private CFG destructIRTernaryOpExpression(IRTernaryOpExpression expr) {
        IRExpression condition = expr.getCondition();

        CFGLine trueBranch = destructIRExpression(expr.getTrueExpression(),"temp_tern_true_" + expr.hashCode()).getStart();
        CFGLine falseBranch = destructIRExpression(expr.getFalseExpression(),"temp_tern_false_" + expr.hashCode()).getStart();

        CFGLine condLine = shortcircuit(condition, trueBranch, falseBranch);

        CFGLine noOp = makeNoOp();
        trueBranch.setNext(noOp);
        falseBranch.setNext(noOp);
        return new CFG(condLine, noOp);
    }

    private CFG destructIRExpression(IRExpression expr) {
        switch(expr.getExpressionType()) {
          case TERNARY: {
            return destructIRTernaryOpExpression((IRTernaryOpExpression) expr);
          }
          case METHOD_CALL: {
            return destructIRMethodCallExpression((IRMethodCallExpression) expr);
          }
          default: {
              String varName = "expr_" + expr.hashCode();
              CFG varExpr = new CFG(new CFGExpression(new IRVariableExpression(varName)));
              return destructIRExpression(expr, varName).concat(varExpr);
          }
        }
    }

    private CFG destructIRMethodCallExpression(IRMethodCallExpression expr) {
        return destructIRStatement(new IRMethodCallStatement(expr));
    }

    private CFG destructIRBlock(IRBlock block) {
        List<IRStatement> statements = block.getStatements();
        List<IRFieldDecl> fieldDecls = block.getFieldDecls();
        CFG f = destructDeclList(fieldDecls);
        CFG s = destructStatementList(statements);
        f.getEnd().setNext(s.getStart());
        return new CFG(f.getStart(), s.getEnd());
    }

    private CFG destructIRAssignStatement(IRAssignStatement stat) {
    	if(stat.getValue().getDepth() == 0) {
    		return new CFG(new CFGStatement(stat));
    	} else {
    		String lastVar = "stat_helper_" + stat.hashCode();
    		CFG expandedExpr = destructIRExpression(stat.getValue(), lastVar);
            IRVariableExpression location = stat.getVarAssigned();
            CFGLine assignLine;
            if (location.isArray()) {
                String locationLastVar = "location_helper_" + location.hashCode();
                CFG expandedIndexExpr = destructIRExpression(location.getIndexExpression(), locationLastVar);
                expandedExpr.getEnd().setNext(expandedIndexExpr.getStart());
                assignLine = new CFGAssignStatement(
                        stat.getVariableName(),
                        locationLastVar,
                        stat.getOperatorToken(),
                        new IRVariableExpression(lastVar));
                expandedIndexExpr.getEnd().setNext(assignLine);
            } else {
        		assignLine = new CFGAssignStatement(stat.getVariableName(), stat.getOperatorToken(), new IRVariableExpression(lastVar));
        		expandedExpr.getEnd().setNext(assignLine);
            }
    		return new CFG(expandedExpr.getStart(), assignLine);
    	}
    }

    /**
     *
     * @param value an expression with depth >0
     * @param lastVar the name to assign to the last temporary variable created
     * @return a CFG where each line is an assign expression of an expression of depth <=1 to a new temporary variable
     */
    private CFG destructIRExpression(IRExpression value, String lastVar) {
        if(value.getExpressionType().equals(ExpressionType.BINARY) && (
                ((IRBinaryOpExpression)value).getOperator().getText().equals("||") ||
                ((IRBinaryOpExpression)value).getOperator().getText().equals("&&"))) {
            IRMemberDecl lastVarDecl = new IRLocalDecl(value.getType(), new CommonToken(lastVar));
            CFG lastVarDeclCFG = destructIRMemberDecl(lastVarDecl);

            CFGLine noOp = makeNoOp();
            CFGLine trueLine = new CFGAssignStatement(lastVar, EQ_OP, new IRBoolLiteral(true));
            CFGLine falseLine = new CFGAssignStatement(lastVar, EQ_OP, new IRBoolLiteral(false));

            trueLine.setNext(noOp);
            falseLine.setNext(noOp);

            CFGLine startShort = shortcircuit(value, trueLine, falseLine);
            return lastVarDeclCFG.concat(new CFG(startShort, noOp));
        }
        CFG answer = new CFG(makeNoOp());
    	int numTempVars = 0;
    	List<IRExpression> tempedChildren = new ArrayList<>();
    	for(IRExpression subExpr: value.getChildren()) {
    		if(subExpr.getDepth() > 0) {
    		    String tempVarName = lastVar + "_" + numTempVars;
    		    numTempVars += 1;
                CFG expandedSubExpr = destructIRExpression(subExpr, tempVarName);
    		    tempedChildren.add(new IRVariableExpression(tempVarName));
    		    answer.concat(expandedSubExpr);
    		} else {
    		    tempedChildren.add(subExpr);
    		}
    	}
        IRMemberDecl lastVarDecl = new IRLocalDecl(value.getType(), new CommonToken(lastVar));
    	CFG lastVarDeclCFG = destructIRMemberDecl(lastVarDecl);
    	IRExpression modifiedValue = putInTempVars(value, tempedChildren);
    	CFGLine lastStatement = new CFGAssignStatement(lastVar, EQ_OP, modifiedValue);
    	answer.concat(lastVarDeclCFG).concat(new CFG(lastStatement));
    	return answer;
    }

    private IRExpression putInTempVars(IRExpression value, List<IRExpression> children) {
	    switch(value.getExpressionType()) {
	    case BINARY: {
	        IRBinaryOpExpression convertedValue = (IRBinaryOpExpression) value;
	        return new IRBinaryOpExpression(children.get(0), convertedValue.getOperator(), children.get(1));
	    }
	    case METHOD_CALL: {
	        IRMethodCallExpression convertedValue = (IRMethodCallExpression) value;
            return new IRMethodCallExpression(convertedValue.getName(), children);
	    }
	    case TERNARY: {
            return new IRTernaryOpExpression(children.get(0), children.get(1), children.get(2));
	    }
	    case UNARY: {
	        IRUnaryOpExpression convertedValue = (IRUnaryOpExpression) value;
            return new IRUnaryOpExpression(convertedValue.getOperator(), children.get(0));
	    }
	    default:
	        return value;
	    }
    }

    private CFG destructDeclList(List<IRFieldDecl> decls) {
        if (decls.size() == 0) {
            CFGLine noOp = makeNoOp();
            return new CFG(noOp);
        }
        CFG firstGraph = destructIRMemberDecl(decls.remove(0));
        CFGLine firstEnd = firstGraph.getEnd();
        CFG restGraph = destructDeclList(decls);
        CFGLine restStart = restGraph.getStart();
        firstEnd.setNext(restStart);
        return new CFG(firstGraph.getStart(), restGraph.getEnd());
        // CFGLine ret = makeNoOp();
        // for (IRMemberDecl decl : decls) {
        //      ret.concat(destructIRMemberDecl(decl));
        // }
    }
    }

    private CFG destructStatementList(List<IRStatement> statements) {
        if (statements.size() == 0) {
            CFGLine noOp = makeNoOp();
            return new CFG(noOp);
        }
        CFG firstGraph = destructIRStatement(statements.remove(0));
        CFGLine firstEnd = firstGraph.getEnd();
        if (firstEnd.getTrueBranch() == null){
            CFG restGraph = destructStatementList(statements);
            CFGLine restStart = restGraph.getStart();
            firstEnd.setNext(restStart);
            return new CFG(firstGraph.getStart(), restGraph.getEnd());
        }
        else {
            // firstEnd had a break/continue statement so we don't evaluate rest of block
            CFGLine endLine = makeNoOp();
            return new CFG(firstGraph.getStart(), endLine);
        }
    }

    private CFGLine shortcircuit(IRExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
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

    private CFGLine shortcircuitAndExpression(IRBinaryOpExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        CFGLine beginSecond = shortcircuit(expr.getRightExpr(), trueBranch, falseBranch);
        CFGLine beginFirst = shortcircuit(expr.getLeftExpr(), beginSecond, falseBranch);
        return beginFirst;
    }

    private CFGLine shortcircuitOrExpression(IRBinaryOpExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        CFGLine beginSecond = shortcircuit(expr.getRightExpr(), trueBranch, falseBranch);
        CFGLine beginFirst = shortcircuit(expr.getLeftExpr(), trueBranch, beginSecond);
        return beginFirst;
    }

    private CFGLine shortcircuitBasicExpression(IRExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        String tempName = "short_temp_" + expr.hashCode();
        CFG exprCFG = destructIRExpression(expr, tempName);
        CFGLine tempExpr = new CFGExpression(trueBranch, falseBranch, new IRVariableExpression(tempName));
        exprCFG.getEnd().setNext(tempExpr);
        return exprCFG.getStart();
    }

    private CFGLine shortcircuitNotExpression(IRUnaryOpExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        CFGLine beginNot = shortcircuit(expr.getArgument(), falseBranch, trueBranch);
        return beginNot;
    }



}

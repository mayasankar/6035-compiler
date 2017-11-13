package edu.mit.compilers.cfg;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRProgram;
import edu.mit.compilers.ir.decl.IRFieldDecl;
import edu.mit.compilers.ir.decl.IRLocalDecl;
import edu.mit.compilers.ir.decl.IRMethodDecl;
import edu.mit.compilers.ir.decl.IRParameterDecl;
import edu.mit.compilers.ir.decl.IRMemberDecl;
import edu.mit.compilers.ir.expression.IRBinaryOpExpression;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRLenExpression;
import edu.mit.compilers.ir.expression.IRMethodCallExpression;
import edu.mit.compilers.ir.expression.IRTernaryOpExpression;
import edu.mit.compilers.ir.expression.IRUnaryOpExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;
import edu.mit.compilers.ir.expression.literal.IRBoolLiteral;
import edu.mit.compilers.ir.expression.literal.IRIntLiteral;
import edu.mit.compilers.ir.expression.literal.IRLiteral;
import edu.mit.compilers.ir.statement.IRAssignStatement;
import edu.mit.compilers.ir.statement.IRStatement;
import edu.mit.compilers.ir.statement.IRBlock;
import edu.mit.compilers.ir.statement.IRForStatement;
import edu.mit.compilers.ir.statement.IRIfStatement;
import edu.mit.compilers.ir.statement.IRLoopStatement;
import edu.mit.compilers.ir.statement.IRMethodCallStatement;
import edu.mit.compilers.ir.statement.IRReturnStatement;
import edu.mit.compilers.ir.statement.IRWhileStatement;

public class CFGCreator2ElectricBoogaloo implements IRNode.IRNodeVisitor<CFG> {

    private CFGProgram program;
    private ExpressionTempNameAssigner namer = new ExpressionTempNameAssigner();
    List<CFGLoopEnv> envStack = new ArrayList<>();


    /**
     * Destructs a CFG for every method in the given program. All lines in the CFG will be valid CFGLines.
     * @param program the IR to destruct
     * @return a map from method name to its CFG
     */
    public static CFGProgram destructCFGsFromIR(IRProgram program) {
        CFGCreator2ElectricBoogaloo creator = new CFGCreator2ElectricBoogaloo();
        creator.program = new CFGProgram(program);
        for (IRMethodDecl method : program.getMethodTable().getMethodList()) {
            CFG methodCFG = method.accept(creator);
            String name = method.getName();
            creator.program.addMethod(name, methodCFG);
        }
        creator.program.blockify();
        
        return creator.program;
    }

    public CFGLine makeNoOp() {
        return new CFGNoOp();
    }

    @Override
    public CFG on(IRProgram ir) {
        throw new RuntimeException("Please call makeCFGsFromIR instead!");
    }

    @Override
    public CFG on(IRFieldDecl ir) {
        throw new RuntimeException("pls dont come up pls dont come up pls dont come up");
    }

    @Override
    public CFG on(IRLocalDecl ir) {
        return onDecl(ir);
    }

    @Override
    public CFG on(IRParameterDecl ir) {
        throw new RuntimeException("pls dont come up pls dont come up pls dont come up");
    }

    private CFG onDecl(IRMemberDecl decl) {
        if(decl.isArray()) {
            CFG returnCFG = new CFG(makeNoOp());
            for(int i=0; i<decl.getLength(); ++i) {
                CFGLine setZero = new CFGAssignStatement2(decl.getName(),
                        new IRIntLiteral(BigInteger.valueOf(i)),
                        new IRIntLiteral(BigInteger.ZERO));
                returnCFG.concat(new CFG(setZero));
            }
            return returnCFG;
        } else {
            return new CFG(new CFGAssignStatement2(decl.getName(), new IRIntLiteral(BigInteger.ZERO)));
        }
    }

    @Override
    public CFG on(IRMethodDecl ir) {
        return ir.getCode().accept(this);
    }

    @Override
    public CFG on(IRUnaryOpExpression ir) {
        String exprTempName = ir.accept(namer);
        if(ir.getDepth() > 1) {
            CFG argumentCFG = ir.getArgument().accept(this);
            IRVariableExpression tempValue = new IRVariableExpression(ir.getArgument().accept(namer));
            IRUnaryOpExpression simplerExpr = new IRUnaryOpExpression(ir.getOperator(), tempValue);
            CFGLine unaryLine = new CFGAssignStatement2(exprTempName, simplerExpr);
            return argumentCFG.concat(new CFG(unaryLine));
        } else {
            return new CFG(new CFGAssignStatement2(exprTempName, ir));
        }
    }

    @Override
    public CFG on(IRBinaryOpExpression ir) {// TODO fix this
        String tempName = ir.accept(namer);
        if(Arrays.asList("&&","||").contains(ir.getOperator().toString())) {
            CFGLine noOp = new CFGNoOp();
            CFGLine trueLine = new CFGAssignStatement2(tempName, new IRBoolLiteral(true));
            CFGLine falseLine = new CFGAssignStatement2(tempName, new IRBoolLiteral(false));
            trueLine.setNext(noOp);
            falseLine.setNext(noOp);

            CFGLine startShort = shortcircuit(ir, trueLine, falseLine);
            return new CFG(startShort, noOp);
        }
        if(ir.getDepth() > 1) {
            CFG returnCFG = new CFG(new CFGNoOp());
            List<IRExpression> exprArgs = new ArrayList<>();

            IRExpression left = ir.getLeftExpr();
            if(left.getDepth() > 0) {
                CFG leftCFG = left.accept(this);
                returnCFG = returnCFG.concat(leftCFG);

                IRVariableExpression name = new IRVariableExpression(left.accept(namer));
                exprArgs.add(name);
            } else {
                exprArgs.add(ir.getLeftExpr());
            }

            IRExpression right = ir.getRightExpr();
            if(right.getDepth() > 0) {
                CFG rightCFG = right.accept(this);
                returnCFG = returnCFG.concat(rightCFG);

                IRVariableExpression name = new IRVariableExpression(right.accept(namer));
                exprArgs.add(name);
            } else {
                exprArgs.add(ir.getRightExpr());
            }

            IRBinaryOpExpression simpleExpr = new IRBinaryOpExpression(exprArgs.get(0), ir.getOperator(), exprArgs.get(1));
            CFGLine exprLine = new CFGAssignStatement2(ir.accept(namer), simpleExpr);
            return returnCFG.concat(new CFG(exprLine));
        } else {
            return new CFG(new CFGAssignStatement2(tempName, ir));
        }
    }

    @Override
    public CFG on(IRTernaryOpExpression ir) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG on(IRLenExpression ir) {
        CFGLine lenLine = new CFGAssignStatement2(ir.accept(namer), ir);
        return new CFG(lenLine);
    }

    @Override
    public CFG on(IRVariableExpression ir) {
        if(ir.getDepth() > 1) {
            //Variable index is complicated
            IRExpression varIndex = ir.getIndexExpression();
            CFG returnCFG = varIndex.accept(this);

            IRVariableExpression indexTempExpr = new IRVariableExpression(varIndex.accept(namer));
            IRVariableExpression simplerExpr = new IRVariableExpression(ir.getName(), indexTempExpr);
            CFGLine varExpr = new CFGAssignStatement2(ir.accept(namer), simplerExpr);

            return returnCFG.concat(new CFG(varExpr));
        } else {
            CFGLine lenLine = new CFGAssignStatement2(ir.accept(namer), ir);
            return new CFG(lenLine);
        }
    }

    @Override
    public CFG on(IRMethodCallExpression ir) {
        if(ir.getDepth() > 1) {
            CFG returnCFG = new CFG(new CFGNoOp());
            List<IRExpression> newArgs = new ArrayList<>();
            for(IRExpression argument: ir.getArguments()) {
                if(argument.getDepth() > 0) {
                    // Method arg must be simplified with a temporary variable
                    CFG argDestruct = argument.accept(this);
                    returnCFG = returnCFG.concat(argDestruct);

                    IRVariableExpression argumentName = new IRVariableExpression(argument.accept(namer));
                    newArgs.add(argumentName);
                } else {
                    newArgs.add(argument);
                }
            }

            IRMethodCallExpression simplerExpr = new IRMethodCallExpression(ir.getName(), newArgs);
            CFGLine varExpr = new CFGAssignStatement2(ir.accept(namer), simplerExpr);

            return returnCFG.concat(new CFG(varExpr));
        } else {
            CFGLine lenLine = new CFGAssignStatement2(ir.accept(namer), ir);
            return new CFG(lenLine);
        }
    }

    @Override
    public CFG onBool(IRLiteral<Boolean> ir) {
        return new CFG(new CFGAssignStatement2(ir.accept(namer), ir));
    }

    @Override
    public CFG onString(IRLiteral<String> ir) {
        return new CFG(new CFGAssignStatement2(ir.accept(namer), ir));
    }

    @Override
    public CFG onInt(IRLiteral<BigInteger> ir) {
        return new CFG(new CFGAssignStatement2(ir.accept(namer), ir));
    }

    @Override
    public CFG on(IRAssignStatement ir) {
        if (ir.getValue().getDepth() == 0) {
    		return new CFG(new CFGAssignStatement2(ir));
    	}
        else {
    		CFG expandedExpr = ir.getValue().accept(this);
            String lastVar = ir.getValue().accept(namer);
            IRVariableExpression location = ir.getVarAssigned();
            CFGLine assignLine;
            if (location.isArray()) {
                CFG expandedIndexExpr = location.getIndexExpression().accept(this);
                String locationLastVar = location.getIndexExpression().accept(namer);
                expandedExpr.getEnd().setNext(expandedIndexExpr.getStart());
                assignLine = new CFGAssignStatement2(ir.getVariableName(), new IRVariableExpression(locationLastVar), new IRVariableExpression(lastVar));
                expandedIndexExpr.getEnd().setNext(assignLine);
            } else {
        		assignLine = new CFGAssignStatement2(ir.getVariableName(), new IRVariableExpression(lastVar));
        		expandedExpr.getEnd().setNext(assignLine);
            }
    		return new CFG(expandedExpr.getStart(), assignLine);
    	}
    }

    // helper; TODO Arkadiy wanted to fix this I think
    private CFG destructStatementList(List<IRStatement> statements) {
        CFG ret = new CFG(makeNoOp());
        for (IRStatement statement : statements) {
            CFG statementGraph = statement.accept(this);
            ret.concat(statementGraph);
            if (statementGraph.getEnd().getTrueBranch() != null) {
                // firstEnd had a break/continue statement so we don't evaluate rest of block
                break;
            }
        }
        return ret;
    }

    @Override
    public CFG on(IRBlock ir) {
        List<IRStatement> statements = ir.getStatements();
        List<IRFieldDecl> fieldDecls = ir.getFieldDecls();
        CFG f = new CFG(makeNoOp());
        for (IRMemberDecl decl : fieldDecls) {
            f.concat(decl.accept(this));
        }
        CFG s = destructStatementList(statements);
        f.getEnd().setNext(s.getStart());
        return new CFG(f.getStart(), s.getEnd());
    }

    @Override
    public CFG on(IRForStatement ir) {
        CFGLine continueNoOp = makeNoOp(); // when we continue, jump to here, which will go to the incrementor
        CFGLine endNoOp = makeNoOp();
        envStack.add(new CFGLoopEnv(continueNoOp, endNoOp));

        IRExpression cond = ir.getCondition();
        IRAssignStatement initializer = ir.getInitializer();
        IRAssignStatement stepFunction = ir.getStepFunction();
        IRBlock block = ir.getBlock();

        CFGAssignStatement2 initLine = new CFGAssignStatement2(initializer);
        CFGAssignStatement2 stepLine = new CFGAssignStatement2(stepFunction);
        CFG blockGraph = block.accept(this);
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

    @Override
    public CFG on(IRIfStatement ir) {
        IRExpression cond = ir.getCondition();
        IRBlock thenBlock = ir.getThenBlock();
        IRBlock elseBlock = ir.getElseBlock();

        List<CFGLoopEnv> envStackCopy = new ArrayList<>(envStack);

        CFG thenGraph = thenBlock.accept(this);
        CFGLine thenStart = thenGraph.getStart();
        CFGLine thenEnd = thenGraph.getEnd();
        CFGLine noOp = makeNoOp();
        thenEnd.setNext(noOp);
        CFGLine condStart;
        if (elseBlock != null) {
            envStack = new ArrayList<>(envStackCopy); // restore, removing any changes we did while destructing child
            CFG elseGraph = elseBlock.accept(this);
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

    @Override
    public CFG on(IRLoopStatement ir) {
        if (ir.getStatementType() == IRStatement.StatementType.BREAK) {
            CFGLine noOp = makeNoOp();
            CFGLoopEnv containingLoop = envStack.get(envStack.size()-1);
            CFGLine followingLine = containingLoop.getFollowingLine();
            noOp.setNext(followingLine);
            return new CFG(noOp);
        }
        else if (ir.getStatementType() == IRStatement.StatementType.CONTINUE) {
            CFGLine noOp = makeNoOp();
            CFGLoopEnv containingLoop = envStack.get(envStack.size()-1);
            CFGLine startLine = containingLoop.getStartLine();
            noOp.setNext(startLine);
            return new CFG(noOp);
        }
        else {
            throw new RuntimeException("Invalid loop statement type: " + ir.getStatementType().toString());
        }
    }

    @Override
    public CFG on(IRMethodCallStatement ir) {
        CFG answer = new CFG(makeNoOp());
    	List<IRExpression> argsWithTemps = new ArrayList<>();
    	for(IRExpression arg: ir.getMethodCall().getArguments()) {
    		if(arg.getDepth() > 0) {
                String tempVarName = arg.accept(namer);
    			CFG expandExpr = arg.accept(this);
    			IRVariableExpression temp = new IRVariableExpression(tempVarName);
    			argsWithTemps.add(temp);
    			answer.concat(expandExpr);
    		} else {
    			argsWithTemps.add(arg);
    		}
    	}
    	IRMethodCallExpression newExpr = new IRMethodCallExpression(ir.getMethodCall().getName(), argsWithTemps);
    	CFG newCFG = new CFG(new CFGMethodCall(newExpr));
    	return answer.concat(newCFG);
    }

    @Override
    public CFG on(IRReturnStatement ir) {
        if (ir.isVoid()) {
            return new CFG(new CFGReturn());
        }
        IRExpression returnExpr = ir.getReturnExpr();
        String name = returnExpr.accept(namer);
        CFG returnCFG = returnExpr.accept(this);
        IRVariableExpression returnVar = new IRVariableExpression(name);
        CFG returnStat = new CFG(new CFGReturn(returnVar));
        return returnCFG.concat(returnStat);
    }

    @Override
    public CFG on(IRWhileStatement ir) {
        CFGLine startNoOp = makeNoOp();
        CFGLine endNoOp = makeNoOp();
        envStack.add(new CFGLoopEnv(startNoOp, endNoOp));

        IRExpression cond = ir.getCondition();
        IRBlock block = ir.getBlock();
        CFG blockGraph = block.accept(this);
        CFGLine blockStart = blockGraph.getStart();
        CFGLine blockEnd = blockGraph.getEnd();
        CFGLine condStart = shortcircuit(cond, blockStart, endNoOp);
        blockEnd.setNext(condStart);
        startNoOp.setNext(condStart);

        envStack.remove(envStack.size()-1);
        return new CFG(startNoOp, endNoOp);
    }

    // SHORT CIRCUITING

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
        CFG exprCFG = expr.accept(this);
        CFGLine tempExpr = new CFGConditional(trueBranch, falseBranch, new IRVariableExpression(expr.accept(namer)));
        exprCFG.getEnd().setNext(tempExpr);
        return exprCFG.getStart();
    }

    private CFGLine shortcircuitNotExpression(IRUnaryOpExpression expr, CFGLine trueBranch, CFGLine falseBranch) {
        CFGLine beginNot = shortcircuit(expr.getArgument(), falseBranch, trueBranch);
        return beginNot;
    }

    private static class ExpressionTempNameAssigner implements IRExpression.IRExpressionVisitor<String> {

        @Override
        public String on(IRUnaryOpExpression ir) {
            return "unary_temp_" + ir.hashCode();
        }

        @Override
        public String on(IRBinaryOpExpression ir) {
            return "binary_temp_" + ir.hashCode();
        }

        @Override
        public String on(IRTernaryOpExpression ir) {
            return "ternary_temp_" + ir.hashCode();
        }

        @Override
        public String on(IRLenExpression ir) {
            return "array_length_temp_" + ir.hashCode();
        }

        @Override
        public String on(IRVariableExpression ir) {
            return "variable_temp_" + ir.hashCode();
        }

        @Override
        public String on(IRMethodCallExpression ir) {
            return "method_call_temp_" + ir.hashCode();
        }

        @Override
        public <T> String on(IRLiteral<T> ir) {
            return "literal_temp_" + ir.hashCode();
        }

    }

}
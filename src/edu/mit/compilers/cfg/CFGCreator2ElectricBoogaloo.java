package edu.mit.compilers.cfg;

import java.math.BigInteger;
import java.util.HashMap;
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

    /**
     * Destructs a CFG for every method in the given program. All lines in the CFG will be valid CFGLines.
     * @param program the IR to destruct
     * @return a map from method name to its CFG
     */
    public static CFGProgram destructCFGsFromIR(IRProgram program) {
        CFGCreator2ElectricBoogaloo creator = new CFGCreator2ElectricBoogaloo();

        for (IRMethodDecl method : program.getMethodTable().getMethodList()) {
            CFG methodCFG = method.accept(creator);
            String name = method.getName();
            creator.program.addMethod(name, methodCFG);
        }

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG on(IRLocalDecl ir) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG on(IRParameterDecl ir) {
        // TODO Auto-generated method stub
        return null;
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
   /*     String exprTempName = ir.accept(namer);
        if(ir.getDepth() > 1) {
            CFG argumentCFG = ir.getArgument().accept(this);
            IRVariableExpression tempValue = new IRVariableExpression(ir.getArgument().accept(namer));
            IRUnaryOpExpression simplerExpr = new IRUnaryOpExpression(ir.getOperator(), tempValue);
            CFGLine unaryLine = new CFGAssignStatement2(exprTempName, simplerExpr);
            return argumentCFG.concat(new CFG(unaryLine));
        } else {
            return new CFG(new CFGAssignStatement2(exprTempName, ir));
        }*/
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG onBool(IRLiteral<Boolean> ir) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG onString(IRLiteral<String> ir) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG onInt(IRLiteral<BigInteger> ir) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG on(IRAssignStatement ir) {
        if (ir.getValue().getDepth() == 0) {
    		return new CFG(new CFGirement(ir));
    	}
        else {
    		CFG expandedExpr = ir.getValue().accept(this);
            IRVariableExpression location = ir.getVarAssigned();
            CFGLine assignLine;
            if (location.isArray()) {
                CFG expandedIndexExpr = location.getIndexExpression().accept(this);
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG on(IRIfStatement ir) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG on(IRLoopStatement ir) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG on(IRMethodCallStatement ir) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG on(IRReturnStatement ir) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG on(IRWhileStatement ir) {
        // TODO Auto-generated method stub
        return null;
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
        String tempName = "short_temp_" + expr.hashCode();
        CFG exprCFG = destructIRExpression(expr, tempName);
        CFGLine tempExpr = new CFGConditional(trueBranch, falseBranch, new IRVariableExpression(tempName));
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

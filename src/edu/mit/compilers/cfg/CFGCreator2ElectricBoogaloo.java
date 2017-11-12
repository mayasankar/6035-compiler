package edu.mit.compilers.cfg;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRProgram;
import edu.mit.compilers.ir.decl.IRFieldDecl;
import edu.mit.compilers.ir.decl.IRLocalDecl;
import edu.mit.compilers.ir.decl.IRMethodDecl;
import edu.mit.compilers.ir.decl.IRParameterDecl;
import edu.mit.compilers.ir.expression.IRBinaryOpExpression;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRLenExpression;
import edu.mit.compilers.ir.expression.IRMethodCallExpression;
import edu.mit.compilers.ir.expression.IRTernaryOpExpression;
import edu.mit.compilers.ir.expression.IRUnaryOpExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;
import edu.mit.compilers.ir.expression.literal.IRLiteral;
import edu.mit.compilers.ir.statement.IRAssignStatement;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG on(IRBlock ir) {
        // TODO Auto-generated method stub
        return null;
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

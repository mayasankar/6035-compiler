package edu.mit.compilers.cfg.optimizations;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.BitSet;
import java.math.BigInteger;

import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.cfg.*;

// so far, just doing mult/div by power of 2 ----> shift operations
public class StrengthReductionVisitor implements IRNode.IRNodeVisitor<Boolean> {

    @Override
    public Boolean on(IRProgram ir){
        return false;
    }

    @Override
    public Boolean on(IRFieldDecl ir){
        return false;
    }
    @Override
    public Boolean on(IRLocalDecl ir){
        return false;
    }
    @Override
    public Boolean on(IRParameterDecl ir){
        return false;
    }
    @Override
    public Boolean on(IRMethodDecl ir){
        return false;
    }

    @Override
    public Boolean on(IRUnaryOpExpression ir){
        return false;
    }
    @Override
    public Boolean on(IRBinaryOpExpression ir){
        // TODO also the case with multiplication where the left term is power of two
        if (ir.getOperator().equals("*") || ir.getOperator().equals("/")) {
            IRExpression rightExpr = ir.getRightExpr();
            if (rightExpr instanceof IRIntLiteral) {
                int val = ((IRIntLiteral)rightExpr).getValue().intValue();
                if (val > 0 && ((val & (val-1)) == 0)) { // if val is a power of two
                    int log = 0;
                    while (val > 1) {
                        val = val >> 1;
                        log += 1;
                    }
                    ir.setOperator(ir.getOperator().equals("*") ? "<<" : ">>");
                    ir.setRightExpr(new IRIntLiteral(BigInteger.valueOf(log)));
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public Boolean on(IRTernaryOpExpression ir){
        return false;
    }
    @Override
    public Boolean on(IRLenExpression ir){
        return false;
    }
    @Override
    public Boolean on(IRVariableExpression ir){
        return false;
    }
    @Override
    public Boolean on(IRMethodCallExpression ir){
        return false;
    }
    @Override
    public Boolean onBool(IRLiteral<Boolean> ir){
        return false;
    }
    @Override
    public Boolean onString(IRLiteral<String> ir){
        return false;
    }
    @Override
    public Boolean onInt(IRLiteral<BigInteger> ir){
        return false;
    }

    @Override
    public Boolean on(IRAssignStatement ir){
        return false;
    }
    @Override
    public Boolean on(IRBlock ir){
        return false;
    }
    @Override
    public Boolean on(IRForStatement ir){
        return false;
    }
    @Override
    public Boolean on(IRIfStatement ir){
        return false;
    }
    @Override
    public Boolean on(IRLoopStatement ir){
        return false;
    }
    @Override
    public Boolean on(IRMethodCallStatement ir){
        return false;
    }
    @Override
    public Boolean on(IRReturnStatement ir){
        return false;
    }
    @Override
    public Boolean on(IRWhileStatement ir){
        return false;
    }

}

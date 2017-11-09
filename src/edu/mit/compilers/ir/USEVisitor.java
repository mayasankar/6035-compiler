package edu.mit.compilers.ir;

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

// return a set of used variables in any IRNode
public class USEVisitor implements IRNode.IRNodeVisitor<Set<String>> {

    @Override
    public Set<String> on(IRProgram ir){
        throw new RuntimeException("Should never call USEVisitor on IRProgram.");
    }

    @Override
    public Set<String> on(IRFieldDecl ir){
        return new HashSet<String>();
    }
    @Override
    public Set<String> on(IRLocalDecl ir){
        return new HashSet<String>();
    }
    @Override
    public Set<String> on(IRParameterDecl ir){
        return new HashSet<String>();
    }
    @Override
    public Set<String> on(IRMethodDecl ir){
        return new HashSet<String>();
    }

    @Override
    public Set<String> on(IRUnaryOpExpression ir){
        IRExpression arg = ir.getArgument();
        return arg.accept(this);
    }
    @Override
    public Set<String> on(IRBinaryOpExpression ir){
        Set<String> ret = new HashSet<>();
        ret.addAll(ir.getLeftExpr().accept(this));
        ret.addAll(ir.getRightExpr().accept(this));
        return ret;
    }
    @Override
    public Set<String> on(IRTernaryOpExpression ir){
        Set<String> ret = new HashSet<>();
        ret.addAll(ir.getTrueExpression().accept(this));
        ret.addAll(ir.getFalseExpression().accept(this));
        ret.addAll(ir.getCondition().accept(this));
        return ret;
    }
    @Override
    public Set<String> on(IRLenExpression ir){
        String lenArgument = ir.getArgument();
        Set<String> ret = new HashSet<>();
        ret.add(lenArgument);
        return ret;
    }
    @Override
    public Set<String> on(IRVariableExpression ir){
        String varName = ir.getName();
        Set<String> ret = new HashSet<>();
        ret.add(varName);
        if (ir.isArray()) {
            ret.addAll(ir.getIndexExpression().accept(this));
        }
        return ret;
    }
    @Override
    public Set<String> on(IRMethodCallExpression ir){
        Set<String> ret = new HashSet<>();
        for (IRExpression arg: ir.getArguments()) {
            ret.addAll(arg.accept(this));
        }
        return ret;
    }
    @Override
    public Set<String> onBool(IRLiteral<Boolean> ir){
        return new HashSet<String>();
    }
    @Override
    public Set<String> onString(IRLiteral<String> ir){
        return new HashSet<String>();
    }
    @Override
    public Set<String> onInt(IRLiteral<BigInteger> ir){
        return new HashSet<String>();
    }

    @Override
    public Set<String> on(IRAssignStatement ir){
        IRExpression value = ir.getValue();
        return value.accept(this);
    }
    @Override
    public Set<String> on(IRBlock ir){
        throw new RuntimeException("Should never call USEVisitor on IRBlock.");
    }
    @Override
    public Set<String> on(IRForStatement ir){
        throw new RuntimeException("Should never call USEVisitor on IRForStatement.");
    }
    @Override
    public Set<String> on(IRIfStatement ir){
        throw new RuntimeException("Should never call USEVisitor on IRIfStatement.");
    }
    @Override
    public Set<String> on(IRLoopStatement ir){
        throw new RuntimeException("Should never call USEVisitor on IRLoopStatement.");
    }
    @Override
    public Set<String> on(IRMethodCallStatement ir){
        // TODO I think these might actually be double-handled by expression generation before the call
        return ir.getMethodCall().accept(this);
    }
    @Override
    public Set<String> on(IRReturnStatement ir){
        if (ir.isVoid()) {
            return new HashSet<String>();
        }
        return ir.getReturnExpr().accept(this);
    }
    @Override
    public Set<String> on(IRWhileStatement ir){
        throw new RuntimeException("Should never call USEVisitor on IRWhileStatement.");
    }

}

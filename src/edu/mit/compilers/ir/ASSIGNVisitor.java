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
public class ASSIGNVisitor implements IRNode.IRNodeVisitor<Set<String>> {

    @Override
    public Set<String> on(IRProgram ir){
        throw new RuntimeException("Should never call USEVisitor on IRProgram.");
    }

    @Override
    public Set<String> on(IRFieldDecl ir){
        Set<String> ret = new HashSet<>();
        ret.add(ir.getName());
        return ret;
    }
    @Override
    public Set<String> on(IRLocalDecl ir){
        Set<String> ret = new HashSet<>();
        ret.add(ir.getName());
        return ret;
    }
    @Override
    public Set<String> on(IRParameterDecl ir){
        Set<String> ret = new HashSet<>();
        ret.add(ir.getName());
        return ret;
    }
    @Override
    public Set<String> on(IRMethodDecl ir){
        return new HashSet<String>();
    }

    @Override
    public Set<String> on(IRUnaryOpExpression ir){
        return new HashSet<String>();
    }
    @Override
    public Set<String> on(IRBinaryOpExpression ir){
        return new HashSet<String>();
    }
    @Override
    public Set<String> on(IRTernaryOpExpression ir){
        return new HashSet<String>();
    }
    @Override
    public Set<String> on(IRLenExpression ir){
        return new HashSet<String>();
    }
    @Override
    public Set<String> on(IRVariableExpression ir){
        return new HashSet<String>();
    }
    @Override
    public Set<String> on(IRMethodCallExpression ir){
        return new HashSet<String>();
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
        Set<String> ret = new HashSet<>();
        ret.add(ir.getVarAssigned().getName());
        return ret;
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
        return new HashSet<String>();
    }
    @Override
    public Set<String> on(IRReturnStatement ir){
        return new HashSet<String>();
    }
    @Override
    public Set<String> on(IRWhileStatement ir){
        throw new RuntimeException("Should never call USEVisitor on IRWhileStatement.");
    }

}

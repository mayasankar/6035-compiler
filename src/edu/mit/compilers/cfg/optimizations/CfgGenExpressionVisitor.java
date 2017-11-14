package edu.mit.compilers.cfg.optimizations;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.math.BigInteger;

import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;
import edu.mit.compilers.cfg.lines.*;

public class CfgGenExpressionVisitor implements CFGLine.CFGVisitor<Set<IRExpression>> {

    //private IRNode.IRNodeVisitor<Set<IRExpression>> USE = new USEVisitor();

	@Override
    public Set<IRExpression> on(CFGBlock line){
        throw new RuntimeException("CfgGenExpressionVisitor should never be called on a CFGBlock.");
    }


    @Override
    public Set<IRExpression> on(CFGAssignStatement line){
        // TODO
        return null;
    }

    @Override
    public Set<IRExpression> on(CFGConditional line){
        // TODO
        return null;
    }

    @Override
    public Set<IRExpression> on(CFGMethodCall line){
        // TODO
        return null;
    }

    @Override
    public Set<IRExpression> on(CFGReturn line){
        // TODO
        return null;
    }

    @Override
    public Set<IRExpression> on(CFGNoOp line){
        return new HashSet<>();
    }
}

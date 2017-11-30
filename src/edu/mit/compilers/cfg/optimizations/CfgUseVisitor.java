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

public class CfgUseVisitor implements CFGLine.CFGVisitor<Set<String>> {

    private IRNode.IRNodeVisitor<Set<String>> USE = new USEVisitor();

	@Override
    public Set<String> on(CFGBlock line){
        // TODO should this do anything other than nothing? I think we never call it on this
        //return false;
        throw new RuntimeException("CfgUseVisitor should never be called on a CFGBlock.");
    }


    @Override
    public Set<String> on(CFGAssignStatement line){
        Set<String> used = new HashSet<>(line.getExpression().accept(USE));
        IRVariableExpression var = line.getVarAssigned();
        if (var.isArray()) {
            used.addAll(var.getIndexExpression().accept(USE));
        }
        return used;
    }

    @Override
    public Set<String> on(CFGBoundsCheck line){
        IRExpression expr = line.getExpression();
        return expr.accept(USE);
    }

    @Override
    public Set<String> on(CFGConditional line){
        IRExpression expr = line.getExpression();
        return expr.accept(USE);
    }

    @Override
    public Set<String> on(CFGMethodCall line){
        IRExpression expr = line.getExpression();
        return expr.accept(USE);
    }

    @Override
    public Set<String> on(CFGReturn line){
        if (line.isVoid()) {
            return new HashSet<>();
        }
        IRExpression expr = line.getExpression();
        return expr.accept(USE);
    }

    @Override
    public Set<String> on(CFGNoOp line){
        return new HashSet<>();
    }
}

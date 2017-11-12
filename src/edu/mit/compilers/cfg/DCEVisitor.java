package edu.mit.compilers.cfg;

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

// boolean returns true if we changed something, false if not
public class DCEVisitor implements CFGLine.CFGVisitor<Boolean> {

    private IRNode.IRNodeVisitor<Set<String>> USE = new USEVisitor();
    private IRNode.IRNodeVisitor<Set<String>> ASSIGN = new ASSIGNVisitor();


    private Boolean onHelper(CFGLine line, Set<String> use, Set<String> assign) {
        // USE + (original + parents - ASSIGN)
        Set<String> original = line.getSetDCE();
        Set<String> setDCE = new HashSet<>(original);
        if (! line.isEnd()){
            // since we're iterating backward the "parents" are the children
            setDCE.addAll(line.getTrueBranch().getSetDCE());
            setDCE.addAll(line.getFalseBranch().getSetDCE());
        }
        setDCE.removeAll(assign);
        setDCE.addAll(use);
        line.setSetDCE(setDCE);
        Boolean changed = !setDCE.equals(original);
        return changed;
    }

	@Override
    public Boolean on(CFGBlock line){
        // TODO should this do anything other than nothing? I think we never call it on this
        //return false;
        throw new RuntimeException("DCEVisitor should never be called on a CFGBlock.");
    }


    @Override
    public Boolean on(CFGAssignStatement2 line){
        Set<String> use = line.getExpression().accept(USE);
        Set<String> assign = line.getVarAssigned().accept(USE);
        return onHelper(line, use, assign);
    }

    @Override
    public Boolean on(CFGConditional line){
        IRExpression expr = line.getExpression();
        Set<String> use = expr.accept(USE);
        Set<String> assign = expr.accept(ASSIGN);
        return onHelper(line, use, assign);
    }

    @Override
    public Boolean on(CFGMethodCall line){
        IRExpression expr = line.getExpression();
        Set<String> use = expr.accept(USE);
        Set<String> assign = expr.accept(ASSIGN);
        return onHelper(line, use, assign);
    }

    @Override
    public Boolean on(CFGReturn line){
        if (line.isVoid()) {
            Set empty = new HashSet<>();
            return onHelper(line, empty, empty);
        }
        IRExpression expr = line.getExpression();
        Set<String> use = expr.accept(USE);
        Set<String> assign = expr.accept(ASSIGN);
        return onHelper(line, use, assign);
    }

    @Override
    public Boolean on(CFGNoOp line){
        Set empty = new HashSet<>();
        return onHelper(line, empty, empty);
    }


    // DELETE BELOW

    @Override
    public Boolean on(CFGStatement line){
        IRStatement statement = line.getStatement();
        Set<String> use = statement.accept(USE);
        Set<String> assign = statement.accept(ASSIGN);
        return onHelper(line, use, assign);
    }

    @Override
    public Boolean on(CFGExpression line){
        IRExpression expr = line.getExpression();
        Set<String> use = expr.accept(USE);
        Set<String> assign = expr.accept(ASSIGN);
        return onHelper(line, use, assign);
    }

    @Override
    public Boolean on(CFGDecl line){
        IRMemberDecl decl = line.getDecl();
        Set<String> use = decl.accept(USE);
        Set<String> assign = decl.accept(ASSIGN);
        return onHelper(line, use, assign);
    }

    @Override
    public Boolean on(CFGMethodDecl line){
        IRMethodDecl decl = line.getMethodDecl();
        Set<String> use = decl.accept(USE);
        Set<String> assign = decl.accept(ASSIGN);
        return onHelper(line, use, assign);
    }

    @Override
    public Boolean on(CFGAssignStatement line){
        IRStatement statement = line.getStatement();
        Set<String> use = statement.accept(USE);
        Set<String> assign = statement.accept(ASSIGN);
        return onHelper(line, use, assign);
    }
}

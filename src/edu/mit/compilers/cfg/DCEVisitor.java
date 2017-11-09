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
public class DCEVisitor implements CFGLine.CFGBitSetVisitor<Boolean> {

    private USEVisitor USE = new USEVisitor();
    private ASSIGNVisitor ASSIGN = new ASSIGNVisitor();


    private Boolean onHelper(CFGLine line, Set<String> parentSet, Set<String> use, Set<String> assign) {
        // USE + (original + parent - ASSIGN)
        Set<String> original = line.getSetDCE();
        Set<String> setDCE = new HashSet<>(original);
        setDCE.addAll(parentSet);
        setDCE.removeAll(assign);
        setDCE.addAll(use);
        line.setSetDCE(setDCE);
        return !setDCE.equals(original);
    }

	@Override
    public Boolean on(CFGBlock line, Set<String> parentSet){
        // TODO should this do anything other than nothing? I think we never call it on this
        //return false;
        throw new RuntimeException("DCEVisitor should never be called on a CFGBlock.");
    }

    @Override
    public Boolean on(CFGStatement line, Set<String> parentSet){
        IRStatement statement = line.getStatement();
        Set<String> use = statement.accept(USE);
        Set<String> assign = statement.accept(ASSIGN);
        return onHelper(line, parentSet, use, assign);
    }

    @Override
    public Boolean on(CFGExpression line, Set<String> parentSet){
        IRExpression expr = line.getExpression();
        Set<String> use = expr.accept(USE);
        Set<String> assign = expr.accept(ASSIGN);
        return onHelper(line, parentSet, use, assign);
    }

    @Override
    public Boolean on(CFGDecl line, Set<String> parentSet){
        IRMemberDecl decl = line.getDecl();
        Set<String> use = decl.accept(USE);
        Set<String> assign = decl.accept(ASSIGN);
        return onHelper(line, parentSet, use, assign);
    }

    @Override
    public Boolean on(CFGMethodDecl line, Set<String> parentSet){
        IRMethodDecl decl = line.getMethodDecl();
        Set<String> use = decl.accept(USE);
        Set<String> assign = decl.accept(ASSIGN);
        return onHelper(line, parentSet, use, assign);
    }

    @Override
    public Boolean on(CFGNoOp line, Set<String> parentSet){
        Set empty = new HashSet<>();
        return onHelper(line, parentSet, empty, empty);
    }

    @Override
    public Boolean on(CFGAssignStatement line, Set<String> parentSet){
        IRStatement statement = line.getStatement();
        Set<String> use = statement.accept(USE);
        Set<String> assign = statement.accept(ASSIGN);
        return onHelper(line, parentSet, use, assign);
    }
}

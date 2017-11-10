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
public class DCEVisitor {

    //private IRNode.IRNodeVisitor<Set<String>> USE = new USEVisitor();
    //private IRNode.IRNodeVisitor<Set<String>> ASSIGN = new ASSIGNVisitor();
    private CFGLine.CFGVisitor<Set<String>> USE = new IRToCFGVisitor(new USEVisitor(), new HashSet<>());
    private CFGLine.CFGVisitor<Set<String>> ASSIGN = new IRToCFGVisitor(new ASSIGNVisitor(), new HashSet<>());

    public void doLivenessAnalysis(CFG cfg) {
        Set<CFGLine> changed = cfg.getAllLines(); // alternatively, begin with predecessors of end
        CFGLine end = cfg.getEnd();
        end.setLivenessIn(end.accept(USE));
        changed.remove(end);

        while (! changed.isEmpty()) {
            CFGLine line = changed.iterator().next(); // TODO can we pop or something?
            changed.remove(line);
            Set<String> newOut = new HashSet<>();
            for (CFGLine child : line.getChildren()) {
                newOut.addAll(child.getLivenessIn());
            }
            Set<String> newIn = new HashSet<>();
            newIn.addAll(line.accept(USE));
            Set<String> newOutDupliate = new HashSet<>(newOut);
            newOutDupliate.removeAll(line.accept(ASSIGN));
            newIn.addAll(newOutDupliate);
            if (! newIn.equals(line.getLivenessIn())) {
                changed.addAll(line.getParents());
            }
            line.setLivenessIn(newIn);
            line.setLivenessOut(newOut);
            //out[line] = union(in[successor] for successor in successors(line))
            //newin[line] = use[line] U (out[line] - def[line])
            //if in[line] != newin[line], add all predecessors of line to changed
        }
    }

    // private Boolean onHelper(CFGLine line, Set<String> use, Set<String> assign) {
    //     // USE + (original + parents - ASSIGN)
    //     Set<String> original = line.getSetDCE();
    //     Set<String> setDCE = new HashSet<>(original);
    //     if (! line.isEnd()){
    //         // since we're iterating backward the "parents" are the children
    //         setDCE.addAll(line.getTrueBranch().getSetDCE());
    //         setDCE.addAll(line.getFalseBranch().getSetDCE());
    //     }
    //     setDCE.removeAll(assign);
    //     setDCE.addAll(use);
    //     line.setSetDCE(setDCE);
    //     Boolean changed = !setDCE.equals(original);
    //     return changed;
    // }
    //
	// @Override
    // public Boolean on(CFGBlock line){
    //     // TODO should this do anything other than nothing? I think we never call it on this
    //     //return false;
    //     throw new RuntimeException("DCEVisitor should never be called on a CFGBlock.");
    // }
    //
    // @Override
    // public Boolean on(CFGStatement line){
    //     IRStatement statement = line.getStatement();
    //     Set<String> use = statement.accept(USE);
    //     Set<String> assign = statement.accept(ASSIGN);
    //     return onHelper(line, use, assign);
    // }
    //
    // @Override
    // public Boolean on(CFGExpression line){
    //     IRExpression expr = line.getExpression();
    //     Set<String> use = expr.accept(USE);
    //     Set<String> assign = expr.accept(ASSIGN);
    //     return onHelper(line, use, assign);
    // }
    //
    // @Override
    // public Boolean on(CFGDecl line){
    //     IRMemberDecl decl = line.getDecl();
    //     Set<String> use = decl.accept(USE);
    //     Set<String> assign = decl.accept(ASSIGN);
    //     return onHelper(line, use, assign);
    // }
    //
    // @Override
    // public Boolean on(CFGMethodDecl line){
    //     IRMethodDecl decl = line.getMethodDecl();
    //     Set<String> use = decl.accept(USE);
    //     Set<String> assign = decl.accept(ASSIGN);
    //     return onHelper(line, use, assign);
    // }
    //
    // @Override
    // public Boolean on(CFGNoOp line){
    //     Set empty = new HashSet<>();
    //     return onHelper(line, empty, empty);
    // }
    //
    // @Override
    // public Boolean on(CFGAssignStatement line){
    //     IRStatement statement = line.getStatement();
    //     Set<String> use = statement.accept(USE);
    //     Set<String> assign = statement.accept(ASSIGN);
    //     return onHelper(line, use, assign);
    // }
}

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

public class CSE implements Optimization {
    private CfgGenExpressionVisitor GEN = new CfgGenExpressionVisitor();

    // everything works in Map<IRExpression, Set<String>>s
    // mapping an available expression to the variables that it is assigned to
    // then to merge two branches into a child, we have Map<IRExpression, Union of its Sets>

    public void optimize(CFGProgram cfgProgram) {
        for (Map.Entry<String, CFG> method : cfgProgram.getMethodToCFGMap().entrySet()) {
            CFG cfg = method.getValue();
            doAvailableExpressionAnalysis(cfg);
            reduceCommonSubexpressions(cfg);
        }
    }

    private void doAvailableExpressionAnalysis(CFG cfg) {
        CFGLine start = cfg.getStart();

        start.setAvailableExpressionsIn(new HashMap<IRExpression, Set<String>>()); // TODO unnecessary remove
        start.setAvailableExpressionsOut(start.accept(GEN));

        // TODO could make it so we assign a temp variable for each intermediate expression
        // so that we can reuse it even if it gets assigned to diff-named variables in diff blocks
        // but that sounds hard and maybe unnecessary

        Set<CFGLine> changed = new HashSet<CFGLine>(cfg.getAllLines());
        changed.remove(start);

        while (!changed.isEmpty()) {
            CFGLine line = changed.iterator().next();
            changed.remove(line);

            Map<IRExpression, Set<String>> newIn = null;
            for (CFGLine parent : line.getParents()) {
                if (newIn == null) {
                    newIn = parent.getAvailableExpressionsOut();
                }
                else {
                    newIn = mergeMaps(newIn, parent.getAvailableExpressionsOut());
                }
            }
            line.setAvailableExpressionsIn(newIn);

            Map<IRExpression, Set<String>> newOut = unionMaps(newIn, line.accept(GEN));
            if (! newOut.equals(line.getAvailableExpressionsOut())) {
                changed.addAll(line.getChildren());
            }
            line.setAvailableExpressionsOut(newOut);
        }
        return;
    }

    private Map<IRExpression, Set<String>> mergeMaps(Map<IRExpression, Set<String>> map1, Map<IRExpression, Set<String>> map2) {
        Map<IRExpression, Set<String>> returnMap = new HashMap<>();
        for (IRExpression key : map1.keySet()) {
            if (map2.containsKey(key)) {
                Set<String> vars = map1.get(key);
                vars.retainAll(map2.get(key));
                returnMap.put(key, vars);
            }
        }
        return returnMap;
    }

    private Map<IRExpression, Set<String>> unionMaps(Map<IRExpression, Set<String>> map1, Map<IRExpression, Set<String>> map2) {
        Map<IRExpression, Set<String>> returnMap = new HashMap<>();
        Set<IRExpression> keySetUnion = map1.keySet();
        keySetUnion.addAll(map2.keySet());
        for (IRExpression key : keySetUnion) {
            if (map1.containsKey(key) && map2.containsKey(key)) {
                Set<String> vars = map1.get(key);
                vars.addAll(map2.get(key));
                returnMap.put(key, vars);
            }
            else if (map1.containsKey(key)){
                Set<String> vars = map1.get(key);
                returnMap.put(key, vars);
            }
            else {
                Set<String> vars = map2.get(key);
                returnMap.put(key, vars);
            }
        }
        return returnMap;
    }

    private void reduceCommonSubexpressions(CFG cfg) {
        throw new RuntimeException("Unimplemented");
    }
    //
    // /**
    //  * returns whether or not dead code has been removed this iteration
    //  */
    // private boolean removeDeadCode(CFG cfg) {
    //     DeadCodeEliminator eliminator = new DeadCodeEliminator(cfg);
    //     Set<CFGLine> toPossiblyRemove = cfg.getAllLines();
    //     boolean changed = false;
    //
    //     for (CFGLine line : toPossiblyRemove) {
    //         changed = changed || line.accept(eliminator);
    //         // if (line.accept(eliminator)) {
    //         //     System.out.println("Removed: " + line.ownValue());
    //         //     changed = true;
    //         // }
    //     }
    //     return changed;
    // }
    //
    // // returns true if gen/kill sets might change, i.e. usually when we have removed a line
    // private class DeadCodeEliminator implements CFGLine.CFGVisitor<Boolean> {
    //     private CFG cfg;
    //
    //     public DeadCodeEliminator(CFG cfg) { this.cfg = cfg; }
    //
    //     @Override
    //     public Boolean on(CFGAssignStatement line) {
    //         // use liveness sets
    //         Set<String> aliveAtEnd = line.getLivenessOut();
    //         Set<String> assigned = line.accept(ASSIGN);
    //         for (String var : assigned) {
    //             if (aliveAtEnd.contains(var)) {
    //                 return false;
    //             }
    //         }
    //         cfg.replaceLine(line, line.getExpression().accept(new LineReplacer(line)));
    //         return true;
    //     }
    //
    //     @Override
    //     public Boolean on(CFGConditional line) {
    //         // remove it if it is not a branch
    //         if (! line.isBranch()) {
    //             cfg.replaceLine(line, line.getExpression().accept(new LineReplacer(line)));
    //             return true;
    //         } else {
    //             return false;
    //         }
    //     }
    //
    //     @Override
    //     public Boolean on(CFGNoOp line) {
    //         // could remove it if it can be condensed out
    //         if (! line.isEnd()) {
    //             cfg.removeLine(line);
    //         }
    //         return false; // even if we're removing a noop, we are not affecting gen/kill sets
    //     }
    //
    //     @Override
    //     public Boolean on(CFGReturn line) {
    //         // TODO is there any case where it could be deleted?
    //         return false;
    //     }
    //
    //     @Override
    //     public Boolean on(CFGMethodCall line) {
    //         // TODO remove it if the method doesn't call any globals
    //         if (line.getExpression().affectsGlobals()) {
    //             return false;
    //         } else {
    //             cfg.replaceLine(line, LineReplacer.getReplacementLine(line));
    //             return true;
    //         }
    //     }
    //
    //     @Override
    //     public Boolean on(CFGBlock line) {
    //         throw new RuntimeException("Eliminating blocks is hard");
    //     }
    // }
    //
    //
    // // if you are deleting a line which evaluates this expression it returns the CFGLine
    // // you want to replace that line with.
    // // returns either CFGMethodCall (if expression is a method call that affects
    // // global variables), or line.getNext() (if line is not end) or new noop (if line is end)
    // private static class LineReplacer implements IRExpression.IRExpressionVisitor<CFGLine> {
    //     private CFGLine line;
    //
    //     public LineReplacer(CFGLine line) { this.line = line; }
    //
    //     public static CFGLine getReplacementLine(CFGLine line) {
    //         if (line.isBranch()) {
    //             throw new RuntimeException("Trying to delete a branch");
    //         }
    //         if (line.isEnd()) {
    //             return new CFGNoOp();
    //         } else {
    //             return line.getTrueBranch();
    //         }
    //     }
    //
    //     @Override
    //     public CFGLine on(IRMethodCallExpression ir) {
    //         if (ir.affectsGlobals()) {
    //             CFGLine newLine = new CFGMethodCall(ir);
    //             newLine.stealChildren(line);
    //             return newLine;
    //         } else {
    //             return getReplacementLine(line);
    //         }
    //     }
    //
    //     @Override
    //     public CFGLine on(IRUnaryOpExpression ir) { return getReplacementLine(line); }
    //     @Override
    //     public CFGLine on(IRBinaryOpExpression ir) { return getReplacementLine(line); }
    //     @Override
    //     public CFGLine on(IRTernaryOpExpression ir) { return getReplacementLine(line); }
    //     @Override
    //     public CFGLine on(IRLenExpression ir) { return getReplacementLine(line); }
    //     @Override
    //     public CFGLine on(IRVariableExpression ir) { return getReplacementLine(line); }
    //     @Override
    //     public <T> CFGLine on(IRLiteral<T> ir) { return getReplacementLine(line); }
    //}

}

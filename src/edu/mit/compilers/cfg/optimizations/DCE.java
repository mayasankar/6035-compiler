package edu.mit.compilers.cfg.optimizations;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.math.BigInteger;

import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;
import edu.mit.compilers.cfg.lines.*;

public class DCE implements Optimization {
    // TODO (mayars) make sure that initializing global variables is added to
    // the CFG for main() because then we don't need to initialize global
    // variables to 0/false if they're initialized later.

    // TODO delete variables that no longer exist from CFGProgram

    private CfgUseVisitor USE = new CfgUseVisitor();
    private CfgAssignVisitor ASSIGN = new CfgAssignVisitor(true);
    private CfgAssignVisitor ASSIGN_NONARRAY = new CfgAssignVisitor(false);

    public boolean optimize(CFGProgram cfgProgram, boolean debug) {
        cfgProgram.recalculateMethodDescriptors();
        boolean anythingChanged = false;
        Set<String> globals = cfgProgram.getGlobalNames();

        for (Map.Entry<String, CFG> method : cfgProgram.getMethodToCFGMap().entrySet()) {
            CFG cfg = method.getValue();
            if (debug) {
                System.out.println("Original CFG:");
                System.out.println(cfg);
            }
            boolean changed = true;
            while (changed) {
                doLivenessAnalysis(cfg, method.getKey().equals("main") ? new HashSet<String>() : globals);
                changed = removeDeadCode(cfg, globals);
                anythingChanged = anythingChanged || changed;
            }
            if (debug) {
                System.out.println("DCE-Optimized CFG:");
                System.out.println(cfg);
            }
        }
        return anythingChanged;
    }

    // TODO make sure that the initial values of everything are valid.
    private void doLivenessAnalysis(CFG cfg, Set<String> globals) {
        CFGLine end = cfg.getEnd();
        end.setLivenessOut(new HashSet<String>(globals));
        Set<String> endIn = new HashSet<String>(globals);
        endIn.addAll(end.accept(USE));
        end.setLivenessIn(endIn);

        // NOTE if this becomes too slow, make changed into a field variable
        // and update it with only the places where things are changed
        Set<CFGLine> changed = new HashSet<CFGLine>(cfg.getAllLines());
        changed.remove(end);

        while (! changed.isEmpty()) {
            CFGLine line = changed.iterator().next();
            changed.remove(line);

            Set<String> newOut = new HashSet<>();
            for (CFGLine child : line.getChildren()) {
                newOut.addAll(child.getLivenessIn());
            }
            Set<String> newIn = new HashSet<>();
            newIn.addAll(line.accept(USE));
            Set<String> newOutDuplicate = new HashSet<>(newOut);
            newOutDuplicate.removeAll(line.accept(ASSIGN_NONARRAY));  // don't want to remove arrays since might be assigning to different index
            newIn.addAll(newOutDuplicate);
            if (! newIn.equals(line.getLivenessIn())) {
                changed.addAll(line.getParents());
            }
            line.setLivenessIn(newIn);
            line.setLivenessOut(newOut);
        }
    }

    /**
     * returns whether or not dead code has been removed this iteration
     */
    private boolean removeDeadCode(CFG cfg, Set<String> globals) {
        DeadCodeEliminator eliminator = new DeadCodeEliminator(cfg, globals);
        Set<CFGLine> toPossiblyRemove = cfg.getAllLines();
        boolean changed = false;

        for (CFGLine line : toPossiblyRemove) {
            changed = changed || line.accept(eliminator);
        }
        return changed;
    }

    // returns true if gen/kill sets might change, i.e. usually when we have removed a line
    private class DeadCodeEliminator implements CFGLine.CFGVisitor<Boolean> {
        private CFG cfg;
        private Set<String> globals;

        public DeadCodeEliminator(CFG cfg, Set<String> globals) { this.cfg = cfg; this.globals = globals; }

        @Override
        public Boolean on(CFGAssignStatement line) {
            // if the assign statement is a[x] = a[x] or b = b then delete it
            if (line.getVarAssigned().equalsExpression(line.getExpression())) {
                cfg.removeLine(line);
                return true;
            }
            // assigning to global variables shouldn't be eliminated since there are side effects
            // use liveness sets
            Set<String> aliveAtEnd = line.getLivenessOut();
            Set<String> assigned = line.accept(ASSIGN);
            for (String var : assigned) {
                if (aliveAtEnd.contains(var) || globals.contains(var)) {
                    return false;
                }
            }
            cfg.replaceLine(line, line.getExpression().accept(new LineReplacer(line)));
            return true;
        }

        @Override
        public Boolean on(CFGBoundsCheck line) {
            // never dead code, because it might throw a bounds check error
            return false;
        }

        @Override
        public Boolean on(CFGConditional line) {
            // remove it if it is not a branch or if the condition is a bool literal
            if (! line.isBranch()) {
                cfg.replaceLine(line, line.getExpression().accept(new LineReplacer(line)));
                return true;
            } else if (line.getExpression().equalsExpression(IRBoolLiteral.TRUE)) {
                cfg.replaceLine(line, line.getTrueBranch());
                return true;
            } else if (line.getExpression().equalsExpression(IRBoolLiteral.FALSE)) {
                cfg.replaceLine(line, line.getFalseBranch());
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Boolean on(CFGNoOp line) {
            // remove it if it can be condensed out
            if (! line.isEnd()) {
                cfg.removeLine(line);
            }
            return false; // even if we're removing a noop, we are not affecting gen/kill sets
        }

        @Override
        public Boolean on(CFGNoReturnError line) {
            // don't remove the line, it will only disappear if it has no parents
            return false;
        }

        @Override
        public Boolean on(CFGReturn line) {
            return false;
        }

        @Override
        public Boolean on(CFGMethodCall line) {
            // remove it if the method doesn't call any globals
            if (line.getExpression().affectsGlobals()) {
                return false;
            }
            cfg.replaceLine(line, LineReplacer.getReplacementLine(line));
            return true;
        }

        @Override
        public Boolean on(CFGBlock line) {
            throw new RuntimeException("Eliminating blocks is hard");
        }
    }



    // if you are deleting a line which evaluates this expression it returns the CFGLine
    // you want to replace that line with.
    // returns either CFGMethodCall (if expression is a method call that affects
    // global variables), or line.getNext() (if line is not end) or new noop (if line is end)
    public static class LineReplacer implements IRExpression.IRExpressionVisitor<CFGLine> {
        private CFGLine line;
        private boolean returnNoOpIfDelete;

        public LineReplacer(CFGLine line, boolean returnNoOp) {
            this.line = line;
            this.returnNoOpIfDelete = returnNoOp;
        }

        public LineReplacer(CFGLine line) { this(line, false); }

        private static CFGLine getReplacementLine(CFGLine line, boolean returnNoOpIfDelete) {
            if (line.isBranch()) {
                throw new RuntimeException("Trying to delete a branch");
            }
            if (returnNoOpIfDelete || line.isEnd()) {
                return new CFGNoOp();
            } else {
                return line.getTrueBranch();
            }
        }

        public static CFGLine getReplacementLine(CFGLine line) {
            return getReplacementLine(line, false);
        }

        private CFGLine getReplacementLine() {
            return getReplacementLine(line, returnNoOpIfDelete);
        }

        @Override
        public CFGLine on(IRMethodCallExpression ir) {
            if (ir.affectsGlobals()) {
                CFGLine newLine = new CFGMethodCall(ir);
                newLine.stealChildren(line);
                return newLine;
            } else {
                return getReplacementLine(line);
            }
        }

        @Override
        public CFGLine on(IRUnaryOpExpression ir) { return getReplacementLine(); }
        @Override // TODO deal with div by 0 case
        public CFGLine on(IRBinaryOpExpression ir) { return getReplacementLine(); }
        @Override
        public CFGLine on(IRTernaryOpExpression ir) { return getReplacementLine(); }
        @Override
        public CFGLine on(IRLenExpression ir) { return getReplacementLine(); }
        @Override
        public CFGLine on(IRVariableExpression ir) { return getReplacementLine(); }
        @Override
        public CFGLine on(IRBoolLiteral ir) { return getReplacementLine(); }
        @Override
        public CFGLine on(IRIntLiteral ir) { return getReplacementLine(); }
        @Override
        public CFGLine on(IRStringLiteral ir) { return getReplacementLine(); }
    }

}

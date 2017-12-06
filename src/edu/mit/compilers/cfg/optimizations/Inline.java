package edu.mit.compilers.cfg.optimizations;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;
import edu.mit.compilers.cfg.lines.*;

// TODO add to Optimizer
public class Inline implements Optimization {
    // NOTE if we don't try to inline a recursive function then we are happy because every local has a unique name

    private Set<String> uninlinedFunctions;
    private CFGProgram cfgProgram;

    public boolean optimize(CFGProgram cp, boolean debug) {
        this.cfgProgram = cp;
        this.uninlinedFunctions = new HashSet<>();
        this.uninlinedFunctions.add("main");
        for (IRMethodDecl method : cfgProgram.getMethodList()) {
            if (method.isImport()) { continue; }
            String methodName = method.getName();
            CFG cfg = cfgProgram.getMethodToCFGMap().get(methodName);
            if (debug) {
                System.out.println("Original CFG:");
                System.out.println(cfg);
            }
            MethodInliner inliner = new MethodInliner(methodName, cfg);
            for (CFGLine line : cfg.getAllLines()) { line.accept(inliner); }
            uninlinedFunctions.addAll(inliner.getUninlinedFunctions());
            if (debug) {
                System.out.println("CFG with inlines:");
                System.out.println(cfg);
            }
        }
        return false; // we only ever want to call this once
    }

    // returns whether inlining happened
    private class MethodInliner implements CFGLine.CFGVisitor<Boolean> {
        private final String currentFunctionName;
        private CFG currentCfg;
        private Set<String> uninlinedFunctionsThisIteration;

        public MethodInliner(String functionName, CFG cfg) {
            this.currentFunctionName = functionName;
            this.currentCfg = cfg;
            this.uninlinedFunctionsThisIteration = new HashSet<>();
        }

        public Set<String> getUninlinedFunctions() { return this.uninlinedFunctionsThisIteration; }

        private boolean shouldNotInline(String methodName) {
            // if inlining becomes too slow, flush this method out
            return methodName.equals(this.currentFunctionName); // no recursive inlining
        }

        public Boolean onMethodCall(CFGLine line, IRVariableExpression varAssigned, IRMethodCallExpression expr) {
            String methodName = expr.getName();
            IRMethodDecl methodDecl = cfgProgram.getMethodParameters(methodName);
            if (methodDecl.isImport()) { // can't inline imports
                return false;
            }
            if (shouldNotInline(methodName)) {
                this.uninlinedFunctionsThisIteration.add(methodName);
                return false;
            }
            // code to set up inlineCFG
            CFG submethodCFG = cfgProgram.getMethodCFG(methodName);
            CFG inlineCFG = makeInlineCFG(submethodCFG,
                                          methodDecl.getReturnType().isVoid(),
                                          varAssigned);
            CFG parameterAssigner = new CFG(new CFGNoOp());
            List<IRMemberDecl> parameters = methodDecl.getParameters().getVariableList();
            List<IRExpression> arguments = expr.getArguments();
            for (int i = 0; i < arguments.size(); ++i) {
                parameterAssigner.addLine(new CFGAssignStatement(parameters.get(i).getName(), arguments.get(i)));
            }
            inlineCFG = parameterAssigner.concat(inlineCFG);
            // now, inline inlineCFG
            this.currentCfg.replaceLineWithCfg(line, inlineCFG);
            return true;
        }

        public Boolean on(CFGMethodCall line) {
            IRVariableExpression varAssigned = null;
            return onMethodCall(line, varAssigned, line.getExpression());
        }
        public Boolean on(CFGAssignStatement line) {
            if (line.getExpression() instanceof IRMethodCallExpression) {
                IRMethodCallExpression expr = (IRMethodCallExpression) line.getExpression(); // TODO fix
                IRVariableExpression varAssigned = line.getVarAssigned();
                return onMethodCall(line, varAssigned, expr);
            } else {
                return onMostLines(line);
            }
        }

        private Boolean onMostLines(CFGLine line) {
            return false;
        }
        public Boolean on(CFGBoundsCheck line) { return onMostLines(line); }
        public Boolean on(CFGConditional line) { return onMostLines(line); }
        public Boolean on(CFGNoOp line) { return onMostLines(line); }
        public Boolean on(CFGNoReturnError line) { return onMostLines(line); }
        public Boolean on(CFGReturn line) { return onMostLines(line); }
        public Boolean on(CFGBlock line) {
            throw new RuntimeException("blockify called prematurely");
        }
    }

    /**
     * Returns a CFG for which return statements are replaced by either NoOps,
     * an IRMethodCallExpression, or varAssigned = returnStatement.expression.
     * parameters: cfg = the original cfg. We will copy it.
     * boolean isVoid: whether or not the function returns void. If it does then
     * its end will be a CFGNoReturnError.
     * varAssigned: is null if isVoid and possibly null otherwise.
     * then we will assign it to a variable which should be killed. DCE will be
     * run directly after every inlining to kill it immediately.
     */
    private CFG makeInlineCFG(CFG cfg, boolean isVoid, IRVariableExpression varAssigned) { // TODO some of the expressions in the copy will be the same, implement IRExpression.copy
        cfg = cfg.copy();
        cfg.addLine(isVoid ? new CFGReturn() : new CFGNoReturnError());
        CFGLine newEnd = new CFGNoOp();
        CFG answerCfg = new CFG(cfg.getStart(), newEnd);
        InlineCFGMaker maker = new InlineCFGMaker(answerCfg, newEnd, varAssigned);
        for (CFGLine line : cfg.getAllLines()) { // don't call maker.on(newend)
            line.accept(maker);
        }
        return answerCfg;
    }

    // always returns true
    private class InlineCFGMaker implements CFGLine.CFGVisitor<Boolean> {
        CFG cfg;
        CFGLine newEnd;
        IRVariableExpression varAssigned;

        public InlineCFGMaker(CFG cfg, CFGLine newEnd, IRVariableExpression varAssigned) {
            this.cfg = cfg;
            this.newEnd = newEnd;
            this.varAssigned = varAssigned;
        }

        private Boolean onMostLines(CFGLine line) {
            if (line.isEnd()) {
                line.setNext(newEnd);
            }
            return true;
        }

        public Boolean on(CFGReturn line) {
            CFGLine replacementLine;
            if (line.isVoid()) {
                replacementLine = new CFGNoOp();
            } else if (varAssigned == null) {
                DCE.LineReplacer replacer = new DCE.LineReplacer(line, true);
                replacementLine = line.getExpression().accept(replacer);
            } else {
                replacementLine = new CFGAssignStatement(varAssigned, line.getExpression());
            }
            replacementLine.setNext(newEnd);
            for (CFGLine child : line.getChildren()) {
                child.removeParentRecursive(line);
            }
            cfg.replaceLine(line, replacementLine);
            return true;
        }

        public Boolean on(CFGAssignStatement line) { return onMostLines(line); }
        public Boolean on(CFGBoundsCheck line) { return onMostLines(line); }
        public Boolean on(CFGConditional line) { return onMostLines(line); }
        public Boolean on(CFGNoOp line) { return onMostLines(line); }
        public Boolean on(CFGNoReturnError line) { return onMostLines(line); }
        public Boolean on(CFGMethodCall line) { return onMostLines(line); }
        public Boolean on(CFGBlock line) {
            throw new RuntimeException("blockify called prematurely");
        }
    }

}

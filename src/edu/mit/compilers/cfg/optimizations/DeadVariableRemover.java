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


// TODO add to optimizer.java

// prevents allocating space for unused variables

public class DeadVariableRemover implements Optimization {
    boolean debug = false;
    CfgUseVisitor USE = new CfgUseVisitor();
    CfgAssignVisitor ASSIGN = new CfgAssignVisitor();

    public boolean optimize(CFGProgram cfgProgram, boolean debug) {
        this.debug = debug;
        if (debug) {
            System.out.println("Original CFGs:");
            for (Map.Entry<String, CFG> method : cfgProgram.getMethodToCFGMap().entrySet()) {
                System.out.println("Method: " + method.getKey());
                System.out.println(method.getValue());
            }
        }
        Set<String> globalsUsed = new HashSet<>();
        for (Map.Entry<String, CFG> method : cfgProgram.getMethodToCFGMap().entrySet()) {
            String methodName = method.getKey();
            CFG cfg = method.getValue();
            Set<String> variablesUsed = getAllVariablesUsed(cfg);
            List<IRMemberDecl> params = cfgProgram.getAllParameters(methodName);
            for (int paramIndex = params.size() - 1; paramIndex >= 0; --paramIndex) {
                if (! variablesUsed.contains(params.get(paramIndex).getName())) {
                    removeParam(paramIndex, methodName, cfgProgram);
                }
            }
            List<VariableDescriptor> locals = cfgProgram.getLocalVariablesForMethod(methodName);
            for (int localIndex = locals.size() - 1; localIndex >= 0; --localIndex) {
                if (! variablesUsed.contains(locals.get(localIndex).getName())) {
                    locals.remove(localIndex);
                }
            }
            globalsUsed.addAll(variablesUsed);
        }
        List<VariableDescriptor> globals = cfgProgram.getGlobalVariables();
        for (int globalIndex = globals.size() - 1; globalIndex >= 0; --globalIndex) {
            if (! globalsUsed.contains(globals.get(globalIndex).getName())) {
                globals.remove(globalIndex);
            }
        }
        if (debug) {
            System.out.println("DVE-optimized CFGs:");
            for (Map.Entry<String, CFG> method : cfgProgram.getMethodToCFGMap().entrySet()) {
                System.out.println("Method: " + method.getKey());
                System.out.println(method.getValue());
            }
        }
        return false;
    }

    private Set<String> getAllVariablesUsed(CFG cfg) {
        Set<String> variablesUsed = new HashSet<>();
        for (CFGLine line : cfg.getAllLines()) {
            variablesUsed.addAll(line.accept(USE));
            variablesUsed.addAll(line.accept(ASSIGN));
        }
        return variablesUsed;
    }

    private void removeParam(int paramIndex, String methodName, CFGProgram cfgProgram) {
        if (debug) {
            System.out.println("Removing " + paramIndex + " from " + methodName);
        }
        // remove that param from its own method's parameter table
        IRMethodDecl decl = cfgProgram.getMethodParameters(methodName);
        decl.getParameters().removeIndex(paramIndex);
        // wherever a cfgline is a method call for that param, remove and replace
        ParameterRemover remover = new ParameterRemover(paramIndex, methodName);
        for (CFG cfg : cfgProgram.getMethodToCFGMap().values()) {
            for (CFGLine line : cfg.getAllLines()) {
                line.accept(remover);
            }
        }
    }

    private class ParameterRemover implements CFGLine.CFGVisitor<Boolean> {
        private int paramIndex;
        private String methodName;

        public ParameterRemover(int i, String name) {
            this.paramIndex = i;
            this.methodName = name;
        }

        private IRMethodCallExpression getReplacement(IRMethodCallExpression old) {
            if (old.getName().equals(methodName)) {
                return new IRMethodCallExpression(old, paramIndex);
            } else {
                return old;
            }
        }

        public Boolean on(CFGAssignStatement line) {
            if (line.getExpression() instanceof IRMethodCallExpression) {
                line.setExpression(getReplacement((IRMethodCallExpression) line.getExpression()));
            }
            return true;
        }
        public Boolean on(CFGBoundsCheck line) { return true; }
    	public Boolean on(CFGConditional line) { return true; }
    	public Boolean on(CFGNoOp line) { return true; }
        public Boolean on(CFGNoReturnError line) { return true; }
    	public Boolean on(CFGReturn line) { return true; }
    	public Boolean on(CFGMethodCall line) {
            line.setExpression(getReplacement(line.getExpression()));
            return true;
        }
        public Boolean on(CFGBlock line) { return true; }
    }

}

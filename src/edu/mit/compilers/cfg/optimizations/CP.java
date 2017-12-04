package edu.mit.compilers.cfg.optimizations;

import java.math.BigInteger;
import java.util.ArrayList;
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

/*
This class implements both copy and constant propagation. Examples:
Copy: b = a; c = b; -> b = a; c = a;
Constant: b = 1 * 2; -> b = 2;

First call doReachingDefinitionsAnalysis. Then call propagate.

LineOptimizer takes in the replacement map and (1) copy-propagates (2) constant-propagates

CopyPropagator does copy propagation with the replacement map. It then does constant
propagation, using constantPropagate().
IntEvaluator and BoolEvaluator evaluate constant int and bool expressions, respectively.
*/

// TODO how should this interface with arrays?
// could optimize the following code
// a[0] = c;
// d = a[0];
// to
// d = c;

// if name = main, can add definitions of globals to 0 to beginning reaching definitions,
// but then globals would have to be added to the use set.

public class CP implements Optimization {
    private CfgGenDefinitionVisitor GEN = new CfgGenDefinitionVisitor();
    private CfgAssignVisitor ASSIGN = new CfgAssignVisitor();

    // TODO decide when splitting is a good idea

    public boolean optimize(CFGProgram cfgProgram, boolean debug) {
        boolean anyCfgChanged = false;
        Set<String> globals = new HashSet<>();
        for (VariableDescriptor var : cfgProgram.getGlobalVariables()) {
            globals.add(var.getName());
        }

        for (Map.Entry<String, CFG> method : cfgProgram.getMethodToCFGMap().entrySet()) {
            CFG cfg = method.getValue();
            if (debug) {
                System.out.println("Original CFG:");
                System.out.println(cfg);
            }
            boolean changed = true;
            while (changed) {
                doReachingDefinitionsAnalysis(cfg);
                changed = propagate(cfg, globals);
                // if (debug) {
                //     System.out.println("CP-Optimized CFG:");
                //     System.out.println(cfg);
                // }
                anyCfgChanged = anyCfgChanged || changed;
            }
            if (debug) {
                System.out.println("CP-Optimized CFG:");
                System.out.println(cfg);
            }
        }
        return anyCfgChanged;
    }

    public static class CPDefinition {
        String varDefined; // TODO make IRVariableExpression and deal with array case
        IRExpression definition;
        Set<String> variablesUsed;

        public CPDefinition(IRVariableExpression varExpr, IRExpression def) {
            varDefined = varExpr.getName();
            definition = def;
            variablesUsed = definition.accept(new USEVisitor());
        }

        public boolean assignsVariable(String otherVar) {
            return varDefined.equals(otherVar);
        }

        public boolean usesVariable(String var) {
            return variablesUsed.contains(var);
        }

        public String getVarDefined() { return varDefined; }
        public IRExpression getDefinition() { return definition; }

        @Override
        public String toString() {
            return varDefined + " = " + definition.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CPDefinition) {
                CPDefinition other = (CPDefinition) obj;
                return varDefined.equals(other.varDefined) && definition.equals(other.definition);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return varDefined.hashCode() + 17 * definition.hashCode();
        }
    }

    private void doReachingDefinitionsAnalysis(CFG cfg) {
        for (CFGLine line : cfg.getAllLines()) {
            line.setReachingDefinitionsOut(new HashMap<CPDefinition,Boolean>());
        }

        CFGLine start = cfg.getStart();
        if (start instanceof CFGBlock) { // TODO refactor so this check is unnecessary
            throw new RuntimeException("This should not happen. Blockify prematurely called.");
        }
        start.setReachingDefinitionsIn(new HashMap<CPDefinition,Boolean>());
        Map<CPDefinition, Boolean> startOut = new HashMap<>();
        for (CPDefinition def : start.accept(GEN)) { startOut.put(def, true); }
        start.setReachingDefinitionsOut(startOut);

        Set<CFGLine> changed = cfg.getAllLines();
        changed.remove(start);

        while (! changed.isEmpty()) {
            CFGLine line = changed.iterator().next();
            changed.remove(line);

            Map<CPDefinition, Boolean> newIn = new HashMap<>();
            // to newIn, add reaching definitions from all parents
            for (CFGLine parent : line.getParents()) {
                for (Map.Entry<CPDefinition, Boolean> kv : parent.getReachingDefinitionsOut().entrySet()) {
                    if (!kv.getValue() || (kv.getValue() && ! newIn.containsKey(kv.getKey()))) {
                        newIn.put(kv.getKey(), kv.getValue());
                    }
                }
            }

            // newout[n] = (in[n] - kill[n]) U gen[n]
            Map<CPDefinition, Boolean> newOut = new HashMap<>(newIn);
            for (String varKilled : line.accept(ASSIGN)) {
                Set<CPDefinition> defs = new HashSet<>(newOut.keySet());
                for (CPDefinition def : defs) {
                    if (def.assignsVariable(varKilled)) {
                        newOut.remove(def);
                    } else if (def.usesVariable(varKilled)) {
                        newOut.put(def, false);
                    }
                }
            }
            for (CPDefinition def : line.accept(GEN)) {
                newOut.put(def, true);
            }

            if (! newOut.equals(line.getReachingDefinitionsOut())) {
                changed.addAll(line.getChildren());
            }
            line.setReachingDefinitionsIn(newIn);
            line.setReachingDefinitionsOut(newOut);
        }
    }

    private boolean propagate(CFG cfg, Set<String> globals) {
        boolean isChanged = false;

        for (CFGLine line : cfg.getAllLines()) {
            Map<CPDefinition, Boolean> reachingDefinitionsIn = line.getReachingDefinitionsIn();
            Map<String, IRExpression> replacementMap = new HashMap<>();
            // replacementMap[var] = null -> don't replace that var
            for (Map.Entry<CPDefinition, Boolean> kv : reachingDefinitionsIn.entrySet()) {
                // should not replace if (1) some replacement is invalid
                // (2) any replacement has depth >= 1 (3) there are multiple replacements
                String var = kv.getKey().getVarDefined();
                IRExpression definition = kv.getKey().getDefinition();
                if (!kv.getValue() || definition.getDepth() != 0 || replacementMap.containsKey(var)) {
                    replacementMap.put(var, null);
                } else {
                    replacementMap.put(var, definition);
                }
            }
            for (String global : globals) { replacementMap.remove(global); }
            // NOTE this section can be removed if we add globals to usevisitors
            USEVisitor USE = new USEVisitor();
            Set<Map.Entry<String, IRExpression>> kvs = new HashSet<>(replacementMap.entrySet());
            for (Map.Entry<String, IRExpression> kv : kvs) {
                if (kv.getValue() == null) {
                    replacementMap.remove(kv.getKey());
                    continue;
                }
                Set<String> globalVarsUsed = kv.getValue().accept(USE);
                globalVarsUsed.retainAll(globals);
                if (!globalVarsUsed.isEmpty()) {
                    replacementMap.remove(kv.getKey());
                }
            }
            LineOptimizer optimizer = new LineOptimizer(replacementMap);
            isChanged = line.accept(optimizer) || isChanged;
        }
        return isChanged;
    }

    // returns whether or not the line is changed
    // runs both copy propagation and constant propagation optimization on the line
    public class LineOptimizer implements CFGLine.CFGVisitor<Boolean> {
        Propagator propagator;

        public LineOptimizer(Map<String, IRExpression> map) {
            propagator = new Propagator(map);
        }

        public Boolean on(CFGAssignStatement line) {
            IRExpression expr = line.getExpression();
            IRExpression newExpr = expr.accept(propagator);
            line.setExpression(newExpr);
            return !(expr.equals(newExpr));
        }

        public Boolean on(CFGBoundsCheck line) {
            return false; // TODO
        }

        public Boolean on(CFGConditional line) {
            IRExpression expr = line.getExpression();
            IRExpression newExpr = expr.accept(propagator);
            line.setExpression(newExpr);
            return !(expr.equals(newExpr));
        }

        public Boolean on(CFGNoOp line) {
            return false;
        }

        public Boolean on(CFGReturn line) {
            if (line.isVoid()) { return false; }
            IRExpression expr = line.getExpression();
            IRExpression newExpr = expr.accept(propagator);
            line.setExpression(newExpr);
            return !(expr.equals(newExpr));
        }

        public Boolean on(CFGMethodCall line) {
            IRMethodCallExpression expr = line.getExpression();
            IRMethodCallExpression newExpr = propagator.onMethodCall(expr);
            line.setExpression(newExpr);
            return !(expr.equals(newExpr));
        }

        public Boolean on(CFGBlock line) {
            throw new RuntimeException("Should not be called");
        }
    }

    private static class Propagator implements IRExpression.IRExpressionVisitor<IRExpression> {
        // for an assign statement that uses a variable that has a single reaching definition
        // and the reaching definition has depth 0
        // replace with that reaching definition
        // else if the assign statement evaluates to a constant, evaluate that constant

        Map<String, IRExpression> replacementMap;

        IntEvaluator intEvaluator = new IntEvaluator();
        BoolEvaluator boolEvaluator = new BoolEvaluator();

        public Propagator(Map<String, IRExpression> replacementMap) {
            this.replacementMap = replacementMap;
        }

        private IRExpression constantPropagate(IRExpression expr) {
            if (expr.isConstant()) {
                TypeDescriptor type = expr.getType();
                if (type == TypeDescriptor.INT) { return new IRIntLiteral(expr.accept(intEvaluator)); }
                else if (type == TypeDescriptor.BOOL) { return new IRBoolLiteral(expr.accept(boolEvaluator)); }
                else { throw new RuntimeException("IRExpression of non int or bool type"); }
            }
            return expr;
        }

        public IRExpression on(IRVariableExpression ir) {
            if (ir.isArray()) {
                IRExpression indexExpression = ir.getIndexExpression().accept(this);
                IRVariableExpression answer = new IRVariableExpression(ir.getName(), indexExpression);
                answer.setType(ir.getType());
                return answer;
            } else {
                IRExpression replacement = replacementMap.get(ir.getName());
                return replacement == null ? ir : replacement;
            }
        }

        public IRExpression on(IRUnaryOpExpression ir) {
            IRExpression newExpr = ir.getArgument().accept(this);
            return constantPropagate(new IRUnaryOpExpression(ir.getOperator(), newExpr));
        }
        public IRExpression on(IRBinaryOpExpression ir) {
            IRExpression leftExpr = ir.getLeftExpr().accept(this);
            IRExpression rightExpr = ir.getRightExpr().accept(this);
            return constantPropagate(new IRBinaryOpExpression(leftExpr, ir.getOperator(), rightExpr));
        }
        public IRExpression on(IRTernaryOpExpression ir) {
            IRExpression condition = ir.getCondition().accept(this);
            IRExpression trueExpression = ir.getTrueExpression().accept(this);
            IRExpression falseExpression = ir.getFalseExpression().accept(this);
            return constantPropagate(new IRTernaryOpExpression(condition, trueExpression, falseExpression));
        }
        public IRExpression on(IRLenExpression ir) {
            throw new RuntimeException("Aah len expressions should have been removed by now");
        }
        public IRMethodCallExpression onMethodCall(IRMethodCallExpression ir) {
            List<IRExpression> args = new ArrayList<>();
            for (IRExpression arg : ir.getArguments()) {
                args.add(arg.accept(this));
            }
            IRMethodCallExpression answer = new IRMethodCallExpression(ir.getName(), args);
            answer.setType(ir.getType());
            return answer;
        }
        public IRExpression on(IRMethodCallExpression ir) {
            return constantPropagate(onMethodCall(ir));
        }
        public IRExpression on(IRBoolLiteral ir) { return ir; }
        public IRExpression on(IRIntLiteral ir) { return ir; }
        public IRExpression on(IRStringLiteral ir) { return ir; }
    }

    private static class BoolEvaluator implements IRExpression.IRExpressionVisitor<Boolean> {
        public Boolean on(IRUnaryOpExpression ir) {
            switch (ir.getOperator()) {
                case "!": return ! ir.getArgument().accept(this);
                default: throw new RuntimeException("Undefined operator " + ir.getOperator() + ".");
            }
        }
        public Boolean on(IRBinaryOpExpression ir) {
            IntEvaluator intEvaluator = new IntEvaluator();
            switch (ir.getOperator()) {
                case "==": {
                    TypeDescriptor type = ir.getLeftExpr().getType();
                    if (type == TypeDescriptor.INT) {
                        return ir.getLeftExpr().accept(intEvaluator).equals(ir.getRightExpr().accept(intEvaluator));
                    } else if (type == TypeDescriptor.BOOL) {
                        return ir.getLeftExpr().accept(this).equals(ir.getRightExpr().accept(this));
                    } else {
                        throw new RuntimeException("Bad expression type");
                    }
                }
                case "!=": {
                    TypeDescriptor type = ir.getLeftExpr().getType();
                    if (type == TypeDescriptor.INT) {
                        return !(ir.getLeftExpr().accept(intEvaluator).equals(ir.getRightExpr().accept(intEvaluator)));
                    } else if (type == TypeDescriptor.BOOL) {
                        return !(ir.getLeftExpr().accept(this).equals(ir.getRightExpr().accept(this)));
                    } else { throw new RuntimeException("Bad expression type"); }
                }
                case "&&": return ir.getLeftExpr().accept(this) && ir.getRightExpr().accept(this);
                case "||": return ir.getLeftExpr().accept(this) || ir.getRightExpr().accept(this);
                case "<": return ir.getLeftExpr().accept(intEvaluator).compareTo(ir.getRightExpr().accept(intEvaluator)) < 0;
                case "<=": return ir.getLeftExpr().accept(intEvaluator).compareTo(ir.getRightExpr().accept(intEvaluator)) <= 0;
                case ">": return ir.getLeftExpr().accept(intEvaluator).compareTo(ir.getRightExpr().accept(intEvaluator)) > 0;
                case ">=": return ir.getLeftExpr().accept(intEvaluator).compareTo(ir.getRightExpr().accept(intEvaluator)) >= 0;
                default: throw new RuntimeException("Undefined operator " + ir.getOperator() + ".");
            }
        }
        public Boolean on(IRTernaryOpExpression ir) {
            if (ir.getCondition().accept(this)) {
                return ir.getTrueExpression().accept(this);
            } else {
                return ir.getFalseExpression().accept(this);
            }
        }
        public Boolean on(IRLenExpression ir) {
            throw new RuntimeException("Len expressions do not return booleans ever");
        }
        public Boolean on(IRVariableExpression ir) {
            throw new RuntimeException("Bool evaluator called on non-constant expression: " + ir.toString());
        }
        public Boolean on(IRMethodCallExpression ir) {
            throw new RuntimeException("Int evaluator called on not-necessarily constant expression: " + ir.toString());
        }
        public Boolean on(IRBoolLiteral ir) {
            return ir.getValue();
        }
        public Boolean on(IRIntLiteral ir) {
            throw new RuntimeException("Bool evaluator called on int literal");
        }
        public Boolean on(IRStringLiteral ir) {
            throw new RuntimeException("Bool evaluator called on string literal");
        }
    }

    private static class IntEvaluator implements IRExpression.IRExpressionVisitor<BigInteger> {
        public BigInteger on(IRUnaryOpExpression ir) {
            switch (ir.getOperator()) {
                case "-": return ir.getArgument().accept(this).negate();
                default: throw new RuntimeException("Undefined operator " + ir.getOperator() + ".");
            }
        }
        public BigInteger on(IRBinaryOpExpression ir) {
            switch (ir.getOperator()) {
                case "+": return ir.getLeftExpr().accept(this).add(ir.getRightExpr().accept(this));
                case "-": return ir.getLeftExpr().accept(this).subtract(ir.getRightExpr().accept(this));
                case "*": return ir.getLeftExpr().accept(this).multiply(ir.getRightExpr().accept(this));
                case "/": return ir.getLeftExpr().accept(this).divide(ir.getRightExpr().accept(this));
                // TODO could use mod instead of remainder, depending on behavior of negative numbers
                case "%": return ir.getLeftExpr().accept(this).remainder(ir.getRightExpr().accept(this));
                default: throw new RuntimeException("Undefined operator " + ir.getOperator() + ".");
            }
        }
        public BigInteger on(IRTernaryOpExpression ir) {
            if (ir.getCondition().accept(new BoolEvaluator())) {
                return ir.getTrueExpression().accept(this);
            } else {
                return ir.getFalseExpression().accept(this);
            }
        }
        public BigInteger on(IRLenExpression ir) {
            throw new RuntimeException("Can't be implemented without a symbol table");
        }
        public BigInteger on(IRVariableExpression ir) {
            throw new RuntimeException("Int evaluator called on non-constant expression: " + ir.toString());
        }
        public BigInteger on(IRMethodCallExpression ir) {
            throw new RuntimeException("Int evaluator called on not-necessarily constant expression: " + ir.toString());
        }
        public BigInteger on(IRBoolLiteral ir) {
            throw new RuntimeException("Int evaluator called on bool literal");
        }
        public BigInteger on(IRIntLiteral ir) {
            return ir.getValue();
        }
        public BigInteger on(IRStringLiteral ir) {
            throw new RuntimeException("Int evaluator called on string literal");
        }
    }

}

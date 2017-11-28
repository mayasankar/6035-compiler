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


// TODO optimize the following code
// a[0] = c;
// d = a[0];
// to
// d = c;

// TODO if name = main, can add definitions of globals to 0 to beginning reaching definitions

public class CP implements Optimization {
    private CfgGenExpressionVisitor GEN = new CfgGenExpressionVisitor();
    private CfgAssignVisitor ASSIGN = new CfgAssignVisitor();
    private USEVisitor IRNodeUSE = new USEVisitor();

    // TODO decide when splitting is a good idea

    public boolean optimize(CFGProgram cfgProgram, boolean debug) {
        boolean anyCfgChanged = false;
        for (Map.Entry<String, CFG> method : cfgProgram.getMethodToCFGMap().entrySet()) {
            CFG cfg = method.getValue();
            if (debug) {
                System.out.println("Original CFG:");
                System.out.println(cfg);
            }
            boolean changed = false;
            do {
                doReachingDefinitionsAnalysis(cfg);
                changed = propagate(cfg);
                System.out.println("CP-Optimized CFG:");
                System.out.println(cfg);
                anyCfgChanged = anyCfgChanged || changed;
            } while (changed);
            if (debug) {
                System.out.println("CP-Optimized CFG:");
                System.out.println(cfg);
            }
        }
        return anyCfgChanged;
    }

    public static class CPDefinition {
        IRVariableExpression varDefined;
        IRExpression definition;
        boolean isValidDefinition = true;

        public CPDefinition(IRVariableExpression vd, IRExpression def) {
            varDefined = vd;
            definition = def;
        }

        public void invalidate() { isValidDefinition = false; }
        public boolean assignsVariable(IRVariableExpression otherVar) {
            // TODO deal with the array case
            return varDefined.equals(otherVar);
        }
        public List<IRVariableExpression> getVariablesUsed() {
            return new ArrayList<>(); // TODO fix
        }

        @Override
        public String toString() {
            return (isValidDefinition ? "VALID " : "INVALID ") + varDefined.toString() + " = " + definition.toString();
        }
    }

    private void doReachingDefinitionsAnalysis(CFG cfg) {
        for (CFGLine line : cfg.getAllLines()) {
            line.setReachingDefinitionsOut(new HashMap<String, Set<IRExpression>>());
        }

        CFGLine start = cfg.getStart();
        if (start instanceof CFGBlock) { // TODO fix this line
            throw new RuntimeException("This should not happen. Blockify prematurely called.");
        }
        // in[start] = emptyset
        start.setReachingDefinitionsIn(new HashMap<String, Set<IRExpression>>());
        // out[start] = gen(start)
        Map<String, Set<IRExpression>> startOut = new HashMap<>();
        for (Map.Entry<IRExpression, Set<String>> kv : start.accept(GEN).entrySet()) {
            for (String varName : kv.getValue()) {
                Set<IRExpression> expressions = startOut.get(varName);
                if (expressions == null) {
                    expressions = new HashSet<>();
                    expressions.add(kv.getKey());
                    startOut.put(varName, expressions);
                } else {
                    expressions.add(kv.getKey());
                }
            }
        }
        start.setReachingDefinitionsOut(startOut);

        // changed = getAllLines() - start
        Set<CFGLine> changed = cfg.getAllLines();
        changed.remove(start);

        // while changed != emptyset {
        while (! changed.isEmpty()) {
            // pop n from changed
            CFGLine line = changed.iterator().next();
            changed.remove(line);

            // in[n] = emptyset
            Map<String, Set<IRExpression>> newIn = new HashMap<>();
            // for all p in parents(n) {
            //     in[n].addAll(out[p])
            // }
            for (CFGLine parent : line.getParents()) { // TODO test
                for (Map.Entry<String, Set<IRExpression>> kv : parent.getReachingDefinitionsOut().entrySet()) {
                    String varName = kv.getKey();
                    Set<IRExpression> expressions = newIn.get(varName);
                    if (expressions == null) {
                        expressions = new HashSet<>();
                        expressions.addAll(kv.getValue());
                        newIn.put(varName, expressions);
                    } else {
                        expressions.addAll(kv.getValue());
                    }
                }
            }

            // newout[n] = gen[n] U (in[n] - kill[n])
            Map<String, Set<IRExpression>> newOut = new HashMap<>();
            for (Map.Entry<String, Set<IRExpression>> kv : newIn.entrySet()) {
                newOut.put(kv.getKey(), new HashSet<IRExpression>(kv.getValue()));
            }
            for (String varKilled : line.accept(ASSIGN)) {
                newOut.remove(varKilled);
            }
            for (Map.Entry<IRExpression, Set<String>> kv : line.accept(GEN).entrySet()) {
                for (String varName : kv.getValue()) {
                    Set<IRExpression> expressions = newOut.get(varName);
                    if (expressions == null) {
                        expressions = new HashSet<>();
                        expressions.add(kv.getKey());
                        newOut.put(varName, expressions);
                    } else {
                        expressions.add(kv.getKey());
                    }
                }
            }

            // if newout[n] != out[n] {
            //     changed.addAll(children(n))
            // }
            if (! newOut.equals(line.getReachingDefinitionsOut())) { // TODO make sure the sets just have to be .equal, not ==
                changed.addAll(line.getChildren());
            }
            // out[n] = newout[n]
            line.setReachingDefinitionsIn(newIn);
            line.setReachingDefinitionsOut(newOut);
        }
    }

    private boolean propagate(CFG cfg) {
        boolean isChanged = false;

        for (CFGLine line : cfg.getAllLines()) {
            Map<String, Set<IRExpression>> reachingDefinitionsIn = line.getReachingDefinitionsIn();
            Map<String, IRExpression> replacementMap = new HashMap<>();
            for (Map.Entry<String, Set<IRExpression>> kv : reachingDefinitionsIn.entrySet()) {
                if (kv.getValue().size() != 1) { continue; }
                IRExpression expr = kv.getValue().iterator().next();
                if (expr.getDepth() == 0) {
                    // BUG but what if the expression has been redefined since then?
                    // e.g. c = a; a = 2; d = c;. Then, we can't replace c with a.
                    // http://www.csd.uwo.ca/~moreno/CS447/Lectures/CodeOptimization.html/node8.html
                    // we also want to mark a reaching definition as invalid in some way
                    replacementMap.put(kv.getKey(), expr);
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

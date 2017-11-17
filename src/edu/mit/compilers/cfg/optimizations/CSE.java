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
    private CfgAssignVisitor ASSIGN = new CfgAssignVisitor();
    private USEVisitor IRNodeUSE = new USEVisitor();

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
            Set<String> killedVars = line.accept(ASSIGN);
            newOut = killVariablesFromMap(killedVars, newOut);

            if (! newOut.equals(line.getAvailableExpressionsOut())) {
                changed.addAll(line.getChildren());
            }
            line.setAvailableExpressionsOut(newOut);
        }
        return;
    }

    // helper for doAvailableExpressionAnalysis
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

    // helper for doAvailableExpressionAnalysis
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

    // helper for doAvailableExpressionAnalysis
    private Map<IRExpression, Set<String>> killVariablesFromMap(Set<String> vars, Map<IRExpression, Set<String>> map) {
        Map<IRExpression, Set<String>> returnMap = new HashMap<>();
        for (IRExpression key : map.keySet()) {
            Set<String> usedVariables = key.accept(IRNodeUSE);
            usedVariables.retainAll(vars);
            if (usedVariables.isEmpty()) {
                // the expression does not use any variables getting killed, so we can add it
                returnMap.put(key, map.get(key));
            }
        }
        return returnMap;
    }

    // returns true if some expressions have been reduced, false if not
    // NOTE we probably don't actually do anything with the return value, but may as well keep it in case future useful
    private boolean reduceCommonSubexpressions(CFG cfg) {
        SubexpressionReducer reducer = new SubexpressionReducer();
        Set<CFGLine> toPossiblyReduce = cfg.getAllLines();
        boolean changed = false;

        for (CFGLine line : toPossiblyReduce) {
            changed = changed || line.accept(reducer);
        }
        return changed;
    }

    // returns true if the line has been reduced
    private class SubexpressionReducer implements CFGLine.CFGVisitor<Boolean> {

        public SubexpressionReducer() {}

        @Override
        public Boolean on(CFGAssignStatement line) {
            IRExpression oldExpr = line.getExpression();
            IRExpression newExpr = reduceExpression(oldExpr, line.getAvailableExpressionsIn());
            if (!oldExpr.equals(newExpr)) {
                line.setExpression(newExpr);
                return true;
            }
            return false;
        }

        @Override
        public Boolean on(CFGConditional line) {
            // depth 0 so doesn't make sense to reduce
            return false;
        }

        @Override
        public Boolean on(CFGNoOp line) {
            return false;
        }

        @Override
        public Boolean on(CFGReturn line) {
            // depth 0 so doesn't make sense to reduce
            return false;
        }

        @Override
        public Boolean on(CFGMethodCall line) {
            // methods may have side effects so we can't necessarily reduce
            return false;
        }

        @Override
        public Boolean on(CFGBlock line) {
            throw new RuntimeException("Reducing blocks is hard.");
        }

        private IRExpression reduceExpression(IRExpression expr, Map<IRExpression, Set<String>> availableExpressions){
            if (availableExpressions.containsKey(expr)) {
                Set<String> varNames = availableExpressions.get(expr);
                if (varNames.isEmpty()) {
                    throw new RuntimeException("availableExpressions should never map to the empty set.");
                }
                String varName = varNames.iterator().next(); // just get one, we don't care which
                IRExpression newExpr = new IRVariableExpression(varName);
                return newExpr;
            }
            return expr;
        }
    }

}

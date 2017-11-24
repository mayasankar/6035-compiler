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

public class CP implements Optimization {
    private CfgGenExpressionVisitor GEN = new CfgGenExpressionVisitor();
    private CfgAssignVisitor ASSIGN = new CfgAssignVisitor();
    private USEVisitor IRNodeUSE = new USEVisitor();

    // TODO decide when splitting is a good idea

    public void optimize(CFGProgram cfgProgram) {
        for (Map.Entry<String, CFG> method : cfgProgram.getMethodToCFGMap().entrySet()) {
            CFG cfg = method.getValue();
            doReachingDefinitionsAnalysis(cfg);
            propagate(cfg);
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
        // for an assign statement that uses a variable that has a single reaching definition
        // replace with that reaching definition

        // else if the assign statement evaluates to a constant, evaluate that constant
        return false;
    }

}

package edu.mit.compilers.cfg.optimizations;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.math.BigInteger;
import java.lang.Integer;

import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;
import edu.mit.compilers.cfg.lines.*;


public class ConflictGraph {
    private Map<String, Set<String>> variableConflicts = new HashMap<>();

    public void addVariable(String var) {
        if(variableConflicts.containsKey(var)) {
            return;
        }
        variableConflicts.put(var, new HashSet<String>());
    }

    public void removeVariable(String var) {
        Set<String> varConflicts = getConflictingVariables(var);
        for (String conf : varConflicts) {
            removeConflict(var, conf);
        }
        variableConflicts.remove(var);
    }

    public void addConflict(String var1, String var2) {
        Set<String> var1Conflicts = getConflictingVariables(var1);
        Set<String> var2Conflicts = getConflictingVariables(var2);
        var1Conflicts.add(var2);
        var2Conflicts.add(var1);
    }

    public void removeConflict(String var1, String var2) {
        Set<String> var1Conflicts = getConflictingVariables(var1);
        Set<String> var2Conflicts = getConflictingVariables(var2);
        var1Conflicts.remove(var2);
        var2Conflicts.remove(var1);
    }

    public Set<String> getVariables() { return variableConflicts.keySet(); }

    public Set<String> getConflictingVariables(String variable) {
        Set<String> result = variableConflicts.get(variable);
        if (result == null) {
            throw new RuntimeException("Attempted to get conflicting variables for a variable not in the set.");
        }
        return result;
    }

    // assigns an integer to each variable such that there are minimal distinct integers; returns this mapping
    // TODO destroys the graph, maybe we should make a copy instead?
    public Map<String, Integer> colorGraph() {
        Map<String, Set<String>> graphCpy = new HashMap<>(variableConflicts);
        Set<String> variables = getVariables();
        List<String> varStack = new ArrayList<>();
        List<Set<String>> conflictStack = new ArrayList<>();

        // go through and always remove the thing with fewest connections, making a stack of vars/conflicts
        while (variables.size() > 0) {
            int minColors = Integer.MAX_VALUE;
            String minColoredVar = null;
            for (String var : variables) {
                int colors = this.getConflictingVariables(var).size();
                if (colors < minColors) {
                    minColors = colors;
                    minColoredVar = var;
                }
            }
            varStack.add(minColoredVar);
            conflictStack.add(this.getConflictingVariables(minColoredVar));
            removeVariable(minColoredVar);
            variables = getVariables();
        }

        // re-add the variables and color MethodAssembler
        Map<String, Integer> coloring = new HashMap<>();
        while (varStack.size() > 0) {
            String var = varStack.remove(varStack.size()-1);
            Set<String> conflicts = conflictStack.remove(conflictStack.size()-1);
            Set<Integer> colorConflicts = new HashSet<>();
            for (String conf: conflicts) {
                if (!coloring.containsKey(conf)) {
                    throw new RuntimeException("Variable being colored has an earlier neighbor uncolored. This should never happen.");
                }
                colorConflicts.add(coloring.get(conf));
            }
            int i = 0;  // set i to be the smallest unused color (greedy)
            while (true) {
                if (!colorConflicts.contains(i)) {
                    break;
                }
                i++;
            }
            coloring.put(var, i);
        }
        variableConflicts = graphCpy;
        return coloring;
    }
    
    @Override
    public String toString() {
        String str = "Variable Conflicts:\n";
        for(String var: variableConflicts.keySet()) {
            str += "Variable " + var +": ";
            for(String conflict: variableConflicts.get(var)) {
                str += conflict + ", ";
            }
            str += "\n";
        }
        return str;
    }

}

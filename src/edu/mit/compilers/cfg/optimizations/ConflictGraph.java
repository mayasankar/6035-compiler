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


public class ConflictGraph {
    private Map<String, Set<String>> variableConflicts = new HashMap<>();

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

    public Set<String> getConflictingVariables(String variable) {
        return variableConflicts.get(variable);
    }

}

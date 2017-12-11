package edu.mit.compilers.cfg.optimizations;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.mit.compilers.assembly.RegisterAllocatedAssigner;
import edu.mit.compilers.cfg.CFG;
import edu.mit.compilers.cfg.CFGLocationAssigner;
import edu.mit.compilers.cfg.CFGProgram;
import edu.mit.compilers.cfg.lines.CFGLine;
import edu.mit.compilers.symbol_tables.VariableDescriptor;

public class RegisterAllocation implements Optimization {

    ConflictGraph graph = new ConflictGraph();

    private CfgUseVisitor USE = new CfgUseVisitor();
    private CfgAssignVisitor ASSIGN = new CfgAssignVisitor(true);
    private CfgAssignVisitor ASSIGN_NONARRAY = new CfgAssignVisitor(false);

	@Override
	public boolean optimize(CFGProgram cp, boolean debug) {
	    for(String method: cp.getMethodNames()) {
	        CFG cfg = cp.getMethodCFG(method);
	        doLivenessAnalysis(cfg, cp.getGlobalNames());
	        for(VariableDescriptor variable: cp.getLocalVariablesForMethod(method)) {
	            graph.addVariable(variable.getName());
	        }
	        for(CFGLine line: cfg.getAllLines()) {
	            Set<String> conflicts = line.getLivenessOut();
	            for(String var: conflicts) {
	                for(String var2: conflicts) {
	                    if(!var.equals(var2)) {
	                        graph.addConflict(var, var2);
	                    }
	                }
	            }
	        }
	    }

	    Map<String, Integer> coloring = graph.colorGraph();
	    CFGLocationAssigner stacker = new RegisterAllocatedAssigner(cp, coloring);
	    cp.setStacker(stacker);
	    return true;
	}

    private void doLivenessAnalysis(CFG cfg, Set<String> globals) { // TODO: add global analysis to register allocation
        CFGLine end = cfg.getEnd();
        end.setLivenessOut(new HashSet<String>());
        Set<String> endIn = new HashSet<String>();
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

}

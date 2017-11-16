package edu.mit.compilers.cfg;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.cfg.optimizations.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.ir.decl.*;

public class VariableStackAssigner {
	private Map<String, VariableDescriptor> variables = new HashMap<>();

	public VariableStackAssigner(CFGProgram program) {
		for (VariableDescriptor var : program.getGlobalVariables()) {
			variables.put(var.getName(), var);
		}

		CfgAssignVisitor ASSIGN = new CfgAssignVisitor();
		Map<String, CFG> methodMap = program.getMethodToCFGMap();
		for (String method : methodMap.keySet()) {
			CFG methodCFG = methodMap.get(method);
			Set<CFGLine> lines = methodCFG.getAllLines();
			int stackPointer = 0;
			for (CFGLine line : lines) {
				Set<String> assignedVariables = line.accept(ASSIGN);
				List<IRMemberDecl> params = program.getAllParameters(method);
				for (IRMemberDecl param : params) {
					assignedVariables.add(param.getName());
				}
				for (String varName : assignedVariables) {
					if (! variables.containsKey(varName)){
						// TODO I'm pretty sure this doesn't work for arrays but not sure how to fix. -jamb
						VariableDescriptor newDescriptor = new VariableDescriptor(varName);
						stackPointer = newDescriptor.pushOntoStack(stackPointer);
						variables.put(varName, newDescriptor);
					}
				}
			}
		}
	}

	public String getAddress(String variableName) {
		VariableDescriptor var = variables.get(variableName);
		if (var == null) {
			throw new RuntimeException("Attempted to access unallocated variable '" + variableName + "'.");
		}
		int offset = var.getStackOffset();
		if (var.isArray()) {
            return "-" + (new Integer(offset).toString()) + "(%rbp, %r10, 8)";
        } else {
            return "-" + (new Integer(offset).toString()) + "(%rbp)";
        }
	}

	public String getMaxSize(String variableName) {
		VariableDescriptor var = variables.get(variableName);
		if (var == null) {
			throw new RuntimeException("Attempted to access unallocated variable '" + variableName + "'.");
		}
		if (var.isArray()) {
			int max_index = var.getLength();
			return "$" + (new Integer(max_index).toString());
        } else {
            throw new RuntimeException("Attempted to call getMaxSize() on non-array variable '" + variableName + "'.");
        }
	}

	public int getNumAllocs() { return this.variables.keySet().size(); }

}

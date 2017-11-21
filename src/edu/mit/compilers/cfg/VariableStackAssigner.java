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
		Map<String, CFG> methodMap = program.getMethodToCFGMap();
		for (String method : methodMap.keySet()) {
			int stackPointer = 0;
			for(VariableDescriptor desc: program.getLocalVariablesForMethod(method)) {
				stackPointer = desc.pushOntoStack(stackPointer);
				variables.put(desc.getName(), desc);
			}
			List<IRMemberDecl> params = program.getAllParameters(method);
			for (IRMemberDecl param : params) {
				VariableDescriptor newDescriptor = new VariableDescriptor(param.getName());
				stackPointer = newDescriptor.pushOntoStack(stackPointer);
				variables.put(param.getName(), newDescriptor);
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

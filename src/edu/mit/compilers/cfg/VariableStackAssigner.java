package edu.mit.compilers.cfg;

import java.util.Map;
import java.util.HashMap;
import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.symbol_tables.*;

public class VariableStackAssigner {
	private Map<String, VariableDescriptor> variables = new HashMap<>();

	public VariableStackAssigner(CFGProgram program) {
		// TODO run through the CFG to figure out which variables will need a space on the stack and assign these
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

}

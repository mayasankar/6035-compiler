package edu.mit.compilers.cfg;

public class VariableStackAssigner {
	
	public VariableStackAssigner(CFGProgram program) {
		// TODO run through the CFG to figure out which variables will need a space on the stack and assign these
	}
	
	public String getAddress(String variableName) {
		return "-17(%rbp)";
	}
}

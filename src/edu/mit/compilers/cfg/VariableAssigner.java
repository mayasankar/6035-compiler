package edu.mit.compilers.cfg;

import edu.mit.compilers.cfg.lines.CFGLine;

public interface VariableAssigner {
	
	/**
	 * Determine where in a register or on the stack a CFGLine should look for a variable
	 * @param line the line being executed
	 * @param variable the variable whose location is needed
	 * @return a register or stack location
	 */
	public String getLocation(CFGLine line, String variable);
}

package edu.mit.compilers.cfg.optimizations;

import java.util.List;

import edu.mit.compilers.cfg.CFGProgram;

public class CFGMutualLivenessGraph {
	
	/**
	 * Runs through a CFGProgram to create a graph mapping each variable to a
	 * list of variables that are alive at the same time as it
	 * @return a CFGMutualLivenessGraph storing the overlap information
	 */
	public static CFGMutualLivenessGraph makeLivenessGraph(CFGProgram program) {
		throw new RuntimeException("not implemented yet");
	}
	
	/**
	 * @return list of all the variables defined at any point in the program
	 */
	public static List<String> getVariables() {
		throw new RuntimeException("not implemented yet");
	}
	/**
	 * 
	 * @param variable the variable we are checking
	 * @return all variables which are live at the same time as variable
	 */
	public static List<String> getOverlappingVariables(String variable) {
		throw new RuntimeException("not implemented yet");
	}
}

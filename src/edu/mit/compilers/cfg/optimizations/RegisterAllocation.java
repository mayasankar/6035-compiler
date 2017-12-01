package edu.mit.compilers.cfg.optimizations;

import edu.mit.compilers.cfg.CFGProgram;

public class RegisterAllocation implements Optimization {

	@Override
	public boolean optimize(CFGProgram cp, boolean debug) {
		CFGMutualLivenessGraph graph = CFGMutualLivenessGraph.makeLivenessGraph(cp);
		return true;
	}

}

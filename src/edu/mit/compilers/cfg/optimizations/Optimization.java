package edu.mit.compilers.cfg.optimizations;

import edu.mit.compilers.cfg.CFGProgram;

public interface Optimization {
    // returns whether or not the CFGProgram is changed
    public boolean optimize(CFGProgram cp);
}

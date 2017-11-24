package edu.mit.compilers.cfg.optimizations;

import edu.mit.compilers.cfg.CFGProgram;

public interface Optimization {
    // TODO make boolean, it should return whether it is changed
    public void optimize(CFGProgram cp);
}

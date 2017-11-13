package edu.mit.compilers.cfg.optimizations;

import edu.mit.compilers.cfg.CFGProgram;

public interface Optimization {
    public void optimize(CFGProgram cp);
}

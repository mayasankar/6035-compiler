package edu.mit.compilers.cfg.optimizations;

import edu.mit.compilers.cfg.CFGProgram;

public class Optimizer {
    // NOTE the ith optimization name must correspond to the ith optimization
    // NOTE the order of optimizations is the order in which they will run
    // repeats are allowed
    public final static String[] optimizationNames = new String[] { "cse", "cp", "dce" };
    public final static Optimization[] optimizations = new Optimization[] { new CSE(), new CP(), new DCE() };

    public static void optimize(CFGProgram cfgProgram, boolean[] opts) {
        for (int i = 0; i < opts.length; ++i) {
            if (! opts[i]) { continue; }
            optimizations[i].optimize(cfgProgram);
        }
    }
}

package edu.mit.compilers.cfg.optimizations;

import edu.mit.compilers.cfg.CFGProgram;

public class Optimizer {
    // NOTE the ith optimization name must correspond to the ith optimization
    // NOTE the order of optimizations is the order in which they will run
    // repeats are allowed
    public final static String[] optimizationNames = new String[] { "cse", "cp", "il", "dce", "ra" }; // TODO reorder il, dce
    public final static Optimization[] optimizations = new Optimization[] { new CSE(), new CP(), new Inline(), new DCE(), new RegisterAllocation() };

    public static void optimize(CFGProgram cfgProgram, boolean[] opts, boolean debug) {
        // TODO make it so that the cycle of optimizations is run until no more optimizing possible.
        for (int i = 0; i < opts.length; ++i) {
            if (! opts[i]) { continue; }
            optimizations[i].optimize(cfgProgram, debug);
        }
    }
}

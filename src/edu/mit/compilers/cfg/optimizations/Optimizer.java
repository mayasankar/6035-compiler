package edu.mit.compilers.cfg.optimizations;

import edu.mit.compilers.cfg.CFGProgram;

public class Optimizer {
    // NOTE the ith optimization name must correspond to the ith optimization
    // NOTE the order of optimizations is the order in which they will run
    // repeats are allowed
    public final static String[] optimizationNames = new String[] { "il", "cse", "cp", "dce", "dve", "ra", "as" };
    public final static Optimization[] optimizations = new Optimization[] { new Inline(), new CSE(), new CP(), new DCE(), new DeadVariableRemover(), new RegisterAllocation(), new CodeSimplifier() };

    public static void optimize(CFGProgram cfgProgram, boolean[] opts, boolean debug) {
        // TODO make it so that the cycle of optimizations is run until no more optimizing possible.
        for (int i = 0; i < opts.length; ++i) {
            if (! opts[i]) { continue; }
            optimizations[i].optimize(cfgProgram, debug);
        }
    }
}

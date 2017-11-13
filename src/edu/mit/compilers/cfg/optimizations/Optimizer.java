package edu.mit.compilers.cfg.optimizations;

import edu.mit.compilers.cfg.CFGProgram;

public class Optimizer {
    //the ith optimization name must correspond to the ith optimization
    public final static String[] optimizationNames = new String[] { "dce" };
    public final static Optimization[] optimizations = new Optimization[] { new DCE() };

    public static void optimize(CFGProgram cfgProgram, boolean[] opts) {
        for (int i = 0; i < opts.length; ++i) {
            if (! opts[i]) { continue; }
            optimizations[i].optimize(cfgProgram);
        }
        cfgProgram.blockify();
    }
}

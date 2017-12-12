package edu.mit.compilers.cfg.optimizations;

import edu.mit.compilers.cfg.CFGProgram;

public class Optimizer {
    // NOTE the ith optimization name must correspond to the ith optimization
    // NOTE the order of optimizations is the order in which they will run
    // repeats are allowed
    public final static String[] optimizationNames = new String[] { "il", "cse", "cp", "dce", "dve", "ra", "as" };
    public final static Optimization[] optimizations = new Optimization[] { new Inline(), new CSE(), new CP(), new DCE(), new DeadVariableRemover(), new RegisterAllocation(), new CodeSimplifier() };

    public static void optimize(CFGProgram cfgProgram, boolean[] opts, boolean debug) {
        // IL
        if (opts[0]) {
            optimizations[0].optimize(cfgProgram, debug);
        }
        // CSE, CP, (DCE then DVR) looping until no changes
        boolean anyChanges = true;
        while (anyChanges) {
            anyChanges = false;
            for (int i = 1; i <= 4; i++) {
                if (opts[i] && i!=2) {
                    anyChanges = anyChanges || optimizations[i].optimize(cfgProgram, debug);
                }
            }
        }
        // RA, AS
        if (opts[5]) {
            optimizations[5].optimize(cfgProgram, debug);
        }
        if (opts[6]) {
            optimizations[6].optimize(cfgProgram, debug);
        }

    }
}

package edu.mit.compilers.cfg.optimizations;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;
import edu.mit.compilers.cfg.lines.*;


// NOTE this is a template class of how to write an optimization
// Also add your optimization to Optimizer.java.
public class ExampleOptimization implements Optimization {
    public void optimize(CFGProgram cfgProgram) {
        for (Map.Entry<String, CFG> method : cfgProgram.getMethodToCFGMap().entrySet()) {
            CFG cfg = method.getValue();
            doAnalysis(cfg);
            simplify(cfg);
        }
    }

    private void doAnalysis(CFG cfg) {

    }

    private void simplify(CFG cfg) {

    }

}

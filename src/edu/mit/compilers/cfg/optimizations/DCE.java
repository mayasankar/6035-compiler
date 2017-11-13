package edu.mit.compilers.cfg.optimizations;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.math.BigInteger;

import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;
import edu.mit.compilers.cfg.lines.*;

public class DCE implements Optimization {
    // TODO (mayars) make sure you keep running DCE until nothing changes.

    private CfgUseVisitor USE = new CfgUseVisitor();
    private CfgAssignVisitor ASSIGN = new CfgAssignVisitor();

    public void optimize(CFGProgram cfgProgram) {
        Set<String> globals = new HashSet<>();
        for (VariableDescriptor var : cfgProgram.getGlobalVariables()) {
            globals.add(var.getName());
        }
        for (CFG cfg : cfgProgram.getAllMethodCFGs()) {
            doLivenessAnalysis(cfg, globals);
            System.out.println("Original CFG:");
            System.out.println(cfg);
            removeDeadCode(cfg);
            System.out.println("DCE-Optimized CFG:");
            System.out.println(cfg);
        }
    }

    private void doLivenessAnalysis(CFG cfg, Set<String> globals) {
        CFGLine end = cfg.getEnd();
        end.setLivenessOut(new HashSet<String>(globals));
        Set<String> endIn = new HashSet<String>(globals);
        endIn.addAll(end.accept(USE));
        end.setLivenessIn(endIn);

        Set<CFGLine> changed = new HashSet<CFGLine>(end.getParents());

        while (! changed.isEmpty()) {
            CFGLine line = changed.iterator().next();
            changed.remove(line);

            Set<String> newOut = new HashSet<>();
            for (CFGLine child : line.getChildren()) {
                newOut.addAll(child.getLivenessIn());
            }
            Set<String> newIn = new HashSet<>();
            newIn.addAll(line.accept(USE));
            Set<String> newOutDuplicate = new HashSet<>(newOut);
            newOutDuplicate.removeAll(line.accept(ASSIGN));
            newIn.addAll(newOutDuplicate);
            if (! newIn.equals(line.getLivenessIn())) {
                changed.addAll(line.getParents());
            }
            line.setLivenessIn(newIn);
            line.setLivenessOut(newOut);
            //out[line] = union(in[successor] for successor in successors(line))
            //newin[line] = use[line] U (out[line] - def[line])
            //if in[line] != newin[line], add all predecessors of line to changed
        }
    }

    private void removeDeadCode(CFG cfg) {
        Set<CFGLine> toPossiblyRemove = cfg.getAllLines();
        for (CFGLine line : toPossiblyRemove) {
            if (! line.isAssign()) {
                continue;
            }
            System.out.println("Considering removing " + line.ownValue());
            boolean removeThisLine = true;
            Set<String> aliveAtEnd = line.getLivenessOut();
            Set<String> assigned = line.accept(ASSIGN);
            for (String var : assigned) {
                if (aliveAtEnd.contains(var)) {
                    removeThisLine = false;
                }
                break;
            }
            if (removeThisLine) {
                System.out.println("Removing " + line.ownValue());
                System.out.println("Before removal:");
                System.out.println(cfg);
                cfg.removeLine(line);
                System.out.println("After removal:");
                System.out.println(cfg);
                System.out.println("\n");
            }
        }
    }

}

package edu.mit.compilers.cfg;

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

// boolean returns true if we changed something, false if not
public class DCEVisitor {

    //private IRNode.IRNodeVisitor<Set<String>> USE = new USEVisitor();
    //private IRNode.IRNodeVisitor<Set<String>> ASSIGN = new ASSIGNVisitor();
    private CFGLine.CFGVisitor<Set<String>> USE = new IRToCFGVisitor(new USEVisitor(), new HashSet<>());
    private CFGLine.CFGVisitor<Set<String>> ASSIGN = new IRToCFGVisitor(new ASSIGNVisitor(), new HashSet<>());

    public void doLivenessAnalysis(CFG cfg) {
        CFGLine end = cfg.getEnd();
        Set<CFGLine> changed = new HashSet<CFGLine>(end.getParents()); // cfg.getAllLines();
        end.setLivenessIn(end.accept(USE));
        // changed.remove(end);

        while (! changed.isEmpty()) {
            CFGLine line = changed.iterator().next(); // TODO can we pop or something?
            changed.remove(line);
            Set<String> newOut = new HashSet<>();
            for (CFGLine child : line.getChildren()) {
                newOut.addAll(child.getLivenessIn());
            }
            Set<String> newIn = new HashSet<>();
            newIn.addAll(line.accept(USE));
            Set<String> newOutDupliate = new HashSet<>(newOut);
            newOutDupliate.removeAll(line.accept(ASSIGN));
            newIn.addAll(newOutDupliate);
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

}

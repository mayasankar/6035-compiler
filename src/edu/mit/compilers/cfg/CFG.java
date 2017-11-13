package edu.mit.compilers.cfg;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedList;
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
import edu.mit.compilers.cfg.lines.*;


// todo list
// multi-line, insert here
// implement getTrueBranch, getFalseBranch

public class CFG {
    private CFGLine start;
    private CFGLine end;

    public CFG(CFGLine start, CFGLine end){
        this.start = start;
        this.end = end;
    }

    public CFG(CFGLine line){
        this.start = line;
        this.end = line;
    }

    public CFGLine getStart() {
        return start;
    }

    public CFGLine getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return start.toString();
    }

    public CFG concat(CFG newEnd) {
        this.end.setNext(newEnd.getStart());
        end = newEnd.getEnd();

        return this;
    }

    public CFG blockify() {
        List<CFGLine> queue = new LinkedList<>();
        queue.add(start);
        while(!queue.isEmpty()) {
            CFGBlock block = new CFGBlock();
            CFGLine firstLine = queue.remove(0);
            
            if(firstLine.getCorrespondingBlock() != null) {
                continue;
            }
            
            updateBlockWithLine(firstLine, block);
            CFGLine line = firstLine;
            while(!line.isBranch() && !line.getTrueBranch().isMerge()) {
                line = line.getTrueBranch();
                updateBlockWithLine(line, block);
            }
            
            queue.add(line.getTrueBranch());
            queue.add(line.getFalseBranch()); 
        }
        
        return new CFG(start.getCorrespondingBlock(), end.getCorrespondingBlock());
    }
    
    private void updateBlockWithLine(CFGLine line, CFGBlock block) {
        block.addLine(line);
        line.setCorrespondingBlock(block);
    }

    public void deadCodeElimination() {
        CFGLine.CFGVisitor<Boolean> visitor = new DCEVisitor();
        Set<CFGLine> toUpdate = new HashSet<>();
        Set<CFGLine> alreadyUpdated = new HashSet<>();
        toUpdate.add(this.end);
        while (! toUpdate.isEmpty()) {
            CFGLine line = toUpdate.iterator().next();
            toUpdate.remove(line);
            alreadyUpdated.add(line);

            // System.out.println("START setDCE:\n");
            // Set<String> dce = line.getSetDCE();
            // for (String elem : dce) {
            //     System.out.println("\t" + elem);
            // }
            Boolean changed = line.accept(visitor);
            // dce = line.getSetDCE();
            // System.out.println("END setDCE:\n");
            // for (String elem : dce) {
            //     System.out.println("\t" + elem);
            // }
            if (changed) {
                alreadyUpdated.removeAll(line.getParents());
            }
            toUpdate.addAll(line.getParents());
            toUpdate.removeAll(alreadyUpdated);
        }

        // iterate through and eliminate dead code
        Set<CFGLine> toPossiblyRemove = new HashSet<>();
        toPossiblyRemove.add(this.end);
        Set<CFGLine> alreadyChecked = new HashSet<>();
        IRNode.IRNodeVisitor<Set<String>> checkAssignments = new ASSIGNVisitor();
        //System.out.println("Removing\n");
        while (! toPossiblyRemove.isEmpty()) {
            CFGLine line = toPossiblyRemove.iterator().next();
            alreadyChecked.add(line);
            toPossiblyRemove.addAll(line.getParents());
            toPossiblyRemove.removeAll(alreadyChecked);
            // System.out.println("To possibly remove:\n");
            // for (CFGLine tpr : toPossiblyRemove) {
            //     System.out.println("\t" + tpr.ownValue());
            // }

            // set of elements assigned in this line; remove the line if there's one and it's dead after
            String assignedVar = "";
            if (line instanceof CFGAssignStatement) {
                assignedVar = ((CFGAssignStatement)line).getVarAssigned().getName();
            }
            //System.out.println("assignedVar: " + assignedVar);
            if (! assignedVar.equals("")){
                // compute the set of variables needed *AFTER* the line - union of DCEs of children
                Set<String> afterSetDCE = new HashSet<>();
                afterSetDCE.addAll(line.getTrueBranch().getSetDCE());
                afterSetDCE.addAll(line.getFalseBranch().getSetDCE());
                // System.out.println("afterSetDCE:\n");
                // for (String tpr : afterSetDCE) {
                //     System.out.println("\t" + tpr);
                // }
                // if you don't have the assigned variable in the live set, your line was dead and you can remove it
                if (!afterSetDCE.contains(assignedVar)) {
                    //continue; // this entire things should do nada
                    // remove by replacing with NOOP
                    CFGLine replacementLine = new CFGNoOp(line.getTrueBranch(), line.getFalseBranch());
                    for (CFGLine p : line.getParents()){
                        p.replaceChildren(line, replacementLine);
                    }
                    line.getTrueBranch().removeParent(line);
                    line.getFalseBranch().removeParent(line);
                }
            }

        }

        return;
    }
}

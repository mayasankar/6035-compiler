package edu.mit.compilers.cfg;

import java.util.List;
import java.util.ArrayList;
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
import edu.mit.compilers.cfg.CFGLine;

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

    public void setValuesDCE() {
        CFGLine.CFGVisitor<Boolean> visitor = new DCEVisitor();
        Set<CFGLine> toUpdate = new HashSet<>();
        toUpdate.add(this.end);
        while (! toUpdate.isEmpty()) {
            CFGLine line = toUpdate.iterator().next();
            Boolean changed = line.accept(visitor);
            if (changed) {
                toUpdate.addAll(line.getParents());
            }
        }
        return;
    }
}

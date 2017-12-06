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

    public void addLine(CFGLine newLine) {
        this.end.setNext(newLine);
        end = newLine;
    }

    public void blockify() { // TODO test
        List<CFGLine> queue = new LinkedList<>();
        queue.add(start);
        while(!queue.isEmpty()) {
            CFGLine firstLine = queue.remove(0);
            if (firstLine.getCorrespondingBlock() != null) {
                continue;
            }
            CFGBlock block = new CFGBlock();
            updateBlockWithLine(firstLine, block);
            CFGLine line = firstLine;
            while(!line.isEnd() && !line.isBranch() && !line.getTrueBranch().isMerge()) {
                line = line.getTrueBranch();
                updateBlockWithLine(line, block);
            }
            queue.addAll(line.getChildren());
        }

        //return new CFG(start.getCorrespondingBlock(), end.getCorrespondingBlock());
    }

    public Set<CFGLine> getAllLines() { // TODO mayars test; also note that after blockifying this returns list of blocks
        Set<CFGLine> toProcess = new HashSet<>();
        toProcess.add(start);
        Set<CFGLine> answer = new HashSet<>();
        while (! toProcess.isEmpty()) {
            CFGLine line = toProcess.iterator().next();
            toProcess.remove(line);
            answer.add(line);
            for (CFGLine nextLine : line.getChildren()) {
                if (! answer.contains(nextLine)) {
                    toProcess.add(nextLine);
                }
            }
        }
        return answer;
    }

    public CFG copy() { // TODO some of the expressions in the copy will be the same, implement IRExpression.copy
        Map<CFGLine, CFGLine> oldToNew = new HashMap<>();
        for (CFGLine oldLine : getAllLines()) {
            if (oldLine.getCorrespondingBlock() != null) {
                throw new RuntimeException("Blockify prematurely called");
            }
            oldToNew.put(oldLine, oldLine.copy());
        }
        for (Map.Entry<CFGLine, CFGLine> oldNew : oldToNew.entrySet()) {
            CFGLine oldLine = oldNew.getKey();
            CFGLine newLine = oldNew.getValue();
            newLine.setBranches(oldToNew.get(oldLine.getTrueBranch()),
                                oldToNew.get(oldLine.getFalseBranch()));
        }
        return new CFG(oldToNew.get(start), oldToNew.get(end));
    }

    // NOTE if newLine is a newly created CFGLine, then you need to call
    // newLine.stealChildren(line) in addition to replaceLine(line, newline)
    // the two commute (probably).
    public void replaceLine(CFGLine line, CFGLine newLine) {
        newLine.removeParent(line);
        for (CFGLine parentLine : line.getParents()) {
            parentLine.replaceChildren(line, newLine);
        }
        if (line == start) { start = newLine; }
        if (line == end) { end = newLine; }
    }

    public void removeLine(CFGLine line) {
        if (line.isBranch()) {
            throw new RuntimeException("trying to remove a branch");
        }
        if (line.isEnd()) {
            replaceLine(line, new CFGNoOp());
        } else {
            replaceLine(line, line.getTrueBranch());
        }
    }

    // replaces a line with a CFG, used for inlining
    public void replaceLineWithCfg(CFGLine line, CFG subCfg) {
        for (CFGLine parentLine : line.getParents()) {
            parentLine.replaceChildren(line, subCfg.start);
        }
        subCfg.end.stealChildren(line);
        if (line == this.start) { this.start = subCfg.start; }
        if (line == this.end) { this.end = subCfg.end; }
    }

    private void updateBlockWithLine(CFGLine line, CFGBlock block) {
        block.addLine(line);
        line.setCorrespondingBlock(block);
    }


}

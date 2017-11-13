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

    public CFG blockify() { // TODO test
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
            while(!line.isEnd() && !line.isBranch() && !line.getTrueBranch().isMerge()) {
                line = line.getTrueBranch();
                updateBlockWithLine(line, block);
            }
            queue.addAll(line.getChildren());
            //queue.add(line.getTrueBranch());
            //queue.add(line.getFalseBranch());
        }

        return new CFG(start.getCorrespondingBlock(), end.getCorrespondingBlock());
    }

    public Set<CFGLine> getAllLines() { // TODO mayars test
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

    // NOTE if newLine is a newly created CFGLine, then you need to call
    // newLine.stealChildren(line) in addition to replaceLine(line, newline)
    // the two commute (probably).
    public void replaceLine(CFGLine line, CFGLine newLine) { // TODO test
        newLine.removeParent(line);
        for (CFGLine parentLine : line.getParents()) {
            parentLine.replaceChildren(line, newLine);
        }
        if (line == start) { start = newLine; }
        if (line == end) { end = newLine; }
    }

    public void removeLine(CFGLine line) { // TODO test
        if (line.isBranch()) {
            throw new RuntimeException("trying to remove a branch");
        }
        if (line.isEnd()) {
            replaceLine(line, new CFGNoOp());
        } else {
            replaceLine(line, line.getTrueBranch());
        }
        // replaces the line with a noop.
        // CFGLine newLine = new CFGNoOp();
        // for (CFGLine parentLine : line.getParents()) {
        //     parentLine.replaceChildren(line, newLine);
        // }
        // newLine.stealChildren(line);
        // if (end == start) { start = newLine; }
        // if (end == line) { end = newLine; }
    }

    private void updateBlockWithLine(CFGLine line, CFGBlock block) {
        block.addLine(line);
        line.setCorrespondingBlock(block);
    }


}

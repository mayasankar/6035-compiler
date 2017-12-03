package edu.mit.compilers.cfg.optimizations;

import java.util.Map;

import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.assembly.lines.*;
import edu.mit.compilers.ir.decl.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class CodeSimplifier {

    // return a simplified version of codeList
    public static List<AssemblyLine> simplifyMovs(List<AssemblyLine> codeList) {
        codeList = removeTrivialMovs(codeList);
        //codeList = simplifyPairMovs(codeList);
        codeList = reduceMovs(codeList);
        codeList = removeTrivialMovs(codeList);
        codeList = eliminatePushPop(codeList);
        return codeList;
    }

    public static List<AssemblyLine> removeTrivialMovs(List<AssemblyLine> l) {
        List<AssemblyLine> codeList = new ArrayList<>(l);

        for (int i=0; i<codeList.size(); i++) {
            if (codeList.get(i) instanceof AMov) {
                AMov mov = (AMov) codeList.get(i);
                String reg1 = mov.getLeft();
                if (mov.getLeft().equals(mov.getRight())) {
                    // useless mov from a thing to itself; delete the line
                    codeList.set(i, new ATrivial());
                }
            }
        }
        return codeList;
    }

    public static List<AssemblyLine> simplifyPairMovs(List<AssemblyLine> l) {
        List<AssemblyLine> codeList = new ArrayList<>(l);

        String lastReg1 = "";
        String lastReg2 = "";
        String reg1 = "";
        String reg2 = "";

        for (int i=0; i<codeList.size(); i++) {
            if (l.get(i) instanceof AMov) {
                AMov mov = (AMov) codeList.get(i);
                reg1 = mov.getLeft();
                reg2 = mov.getRight();
                // if we had a move on the last line, and the register
                // moved into is the same as the first one we move now,
                // and the middle thing being simplified out is a register
                // and not both others were on the stack, simplify
                if (!lastReg1.equals("") && lastReg2.equals(reg1) && lastReg2.startsWith("%")
                    && (lastReg1.startsWith("%") || reg2.startsWith("%"))) {
                    // useless mov from a thing to itself; delete the line
                    codeList.set(i-1, new AWhitespace());
                    codeList.set(i, new AMov(lastReg1, reg2));
                    reg1 = lastReg1;
                }
            }
            else {
                reg1 = "";
                reg2 = "";
            }
            lastReg1 = reg1;
            lastReg2 = reg2;
        }
        return codeList;
    }

    private static Integer readIndex(Map<String, Integer> readIndices, String reg) {
        if (readIndices.get(reg) == null) {
            if (readIndices.get("ALL") == null) {
                return -1;
            }
            return readIndices.get("ALL");
        }
        if (readIndices.get("ALL") == null) {
            return readIndices.get(reg);
        }
        if (readIndices.get("ALL") > readIndices.get(reg)) {
            return readIndices.get("ALL");
        }
        return readIndices.get(reg);
    }

    // if I store something in %x, then don't touch it or %y before doing mov %x %y,
    // and overwrite it without doing anything else to it, eliminate it
    public static List<AssemblyLine> reduceMovs(List<AssemblyLine> l) {
        // map registers to when they were most recently read and written
        // if we have (1) write to reg X (2) mov X, Y (3) no reads/writes of X or Y in between 1 and 2,
        // can simplify to write-to-reg-Y
        // NOTE this assumes we don't ever read X again. this is valid for our code, but if we change stuff, may break
        // also, if we write to X and don't read it before the next write, eliminate

        List<AssemblyLine> codeList = new ArrayList<>(l);
        AssemblyLine.AssemblyLineVisitor<Set<String>> writeVisitor = new WritesToRegisters();
        AssemblyLine.AssemblyLineVisitor<Set<String>> readVisitor = new ReadsFromRegisters();

        Map<String, Integer> recentReadLine = new HashMap<>();
        Map<String, Integer> recentWriteLine = new HashMap<>();

        for (int i=0; i<codeList.size(); i++) {
            AssemblyLine line = codeList.get(i);
            Set<String> writesTo = line.accept(writeVisitor);
            Set<String> readsFrom = line.accept(readVisitor);

            if (line instanceof AMov) {
                AMov movLine = (AMov)line;
                String rreg = movLine.getRight();
                String lreg = movLine.getLeft();
                if (movLine.leftIsRegister() && movLine.rightIsRegister()) {
                    Integer lastLeftWrite = recentWriteLine.get(lreg);
                    Integer lastRightWrite = recentWriteLine.get(rreg);
                    if (lastRightWrite == null) {
                        lastRightWrite = -1;
                    }
                    Integer lastLeftRead = readIndex(recentReadLine, lreg);
                    Integer lastRightRead = readIndex(recentReadLine, rreg);
                    if (lastLeftWrite != null && lastLeftWrite >= lastLeftRead && lastLeftWrite >= lastRightRead && lastLeftWrite > lastRightWrite) {
                        AssemblyLine.AssemblyLineVisitor<Boolean> changeWriteVisitor = new ChangeWrites(rreg);
                        if (codeList.get(lastLeftWrite).accept(changeWriteVisitor)) {
                            codeList.set(i, new ACommand("# formerly mov " + lreg + ", " + rreg));
                        }
                    }
                }
            }
            for (String reg : readsFrom) {
                recentReadLine.put(reg, i);
            }
            for (String reg : writesTo) {
                Integer lastPossibleRead = readIndex(recentReadLine, reg);
                Integer lastWrite = recentWriteLine.get(reg);
                if (lastWrite != null && lastWrite > lastPossibleRead) {
                    // there was no point doing the write
                    codeList.set(lastWrite, new ACommand("# formerly wrote to " + reg));
                }
                recentWriteLine.put(reg, i);
            }

        }
        return codeList;
    }

    // if I push a register and pop it back without using the register meanwhile, remove
    public static List<AssemblyLine> eliminatePushPop(List<AssemblyLine> l) {
        // stack of push operations, stack of their indices
        // stack of <regs written to since most recent push, possibly including ALL if jmp>
        // upon POP, remove the top things from push stack and index stack, see if we can simplify it,
        //      and pop the top thing from regs written (rep'd as separate item) and add all its contents to the next-to-top thing
        List<APush> pushOperations = new ArrayList<>();
        List<Integer> pushIndices = new ArrayList<>();
        List<Set<String>> regsWrittenSinceLastPush = new ArrayList<>();
        Set<String> regsWritten = new HashSet<>();
        AssemblyLine.AssemblyLineVisitor<Set<String>> writesTo = new WritesToRegisters();

        List<AssemblyLine> codeList = new ArrayList<>(l);
        for (int i=0; i<codeList.size(); i++) {
            AssemblyLine line = codeList.get(i);
            if (line instanceof APush) {
                pushOperations.add((APush)line);
                pushIndices.add(i);
                regsWrittenSinceLastPush.add(regsWritten);
                regsWritten = new HashSet<>();
            }
            else if (line instanceof APop) {
                APush push = pushOperations.remove(pushOperations.size()-1);
                Integer index = pushIndices.remove(pushIndices.size()-1);
                String regPush = push.getReg();
                // check ALL in case there was a jmp or something else we can't be sure of in between
                if (!regsWritten.contains("ALL") && !regsWritten.contains(regPush)) {
                    // we pushed a register and then never wrote anything else to it; we can remove that
                    // but we should check if it's popping to a different register
                    String regPop = ((APop)line).getReg();
                    if (!regPop.equals(regPush)) {
                        codeList.set(index, new ACommand("# formerly push " + regPush));
                        codeList.set(i, new AMov(regPush, regPop));
                    }
                    codeList.set(index, new ACommand("# formerly push " + regPush));
                    codeList.set(i, new ACommand("# formerly pop " + regPop));
                }
                if (regsWrittenSinceLastPush.size() > 0) {
                    regsWritten.addAll(regsWrittenSinceLastPush.remove(regsWrittenSinceLastPush.size()-1));
                }
            }
            else {
                regsWritten.addAll(line.accept(writesTo));
            }
        }
        return codeList;
    }


    // return the set of registers that MIGHT have something written to them
    private static class WritesToRegisters implements AssemblyLine.AssemblyLineVisitor<Set<String>> {
        @Override
        public Set<String> on(ACall line) {
            return new HashSet<String>(Arrays.asList("ALL"));
        }

        @Override
        public Set<String> on(ACmov line) {
            if (!line.rightIsRegister()) {
                return new HashSet<String>();
            }
            return new HashSet<String>(Arrays.asList(line.getRight()));
        }

        @Override
        public Set<String> on(ACmp line) { return new HashSet<String>(); }

        @Override
        public Set<String> on(ACommand line) { return new HashSet<String>(); }

        @Override
        public Set<String> on(AJmp line) {
            return new HashSet<String>(Arrays.asList("ALL"));
        }

        @Override
        public Set<String> on(ALabel line) { return new HashSet<String>(); }

        @Override
        public Set<String> on(AMov line) {
            if (!line.rightIsRegister()) {
                return new HashSet<String>();
            }
            return new HashSet<String>(Arrays.asList(line.getRight()));
        }

        @Override
        public Set<String> on(AOps line) {
            return new HashSet<String>(Arrays.asList(line.getRight()));
        }

        @Override
        public Set<String> on(APop line) {
            return new HashSet<String>(Arrays.asList(line.getReg()));
        }

        @Override
        public Set<String> on(APush line) { return new HashSet<String>(); }

        @Override
        public Set<String> on(AShift line) {
            return new HashSet<String>(Arrays.asList(line.getReg()));
        }

        @Override
        public Set<String> on(AString line) { return new HashSet<String>(); }

        @Override
        public Set<String> on(ATrivial line) { return new HashSet<String>(); }

        @Override
        public Set<String> on(AUnaryOp line) {
            return new HashSet<String>(Arrays.asList(line.getReg()));
        }

        @Override
        public Set<String> on(AWhitespace line) { return new HashSet<String>(); }
    }

    // return the set of registers that MIGHT be read
    private static class ReadsFromRegisters implements AssemblyLine.AssemblyLineVisitor<Set<String>> {
        @Override
        public Set<String> on(ACall line) {
            return new HashSet<String>(Arrays.asList("ALL"));
        }

        @Override
        public Set<String> on(ACmov line) {
            if (!line.leftIsRegister()) {
                return new HashSet<String>();
            }
            return new HashSet<String>(Arrays.asList(line.getLeft()));
        }

        @Override
        public Set<String> on(ACmp line) {
            return new HashSet<String>(Arrays.asList(line.getLeft(), line.getRight()));
        }

        @Override
        public Set<String> on(ACommand line) { return new HashSet<String>(); }

        @Override
        public Set<String> on(AJmp line) {
            return new HashSet<String>(Arrays.asList("ALL"));
        }

        @Override
        public Set<String> on(ALabel line) { return new HashSet<String>(); }

        @Override
        public Set<String> on(AMov line) {
            if (!line.leftIsRegister()) {
                return new HashSet<String>();
            }
            return new HashSet<String>(Arrays.asList(line.getLeft()));
        }

        @Override
        public Set<String> on(AOps line) {
            return new HashSet<String>(Arrays.asList(line.getLeft()));
        }

        @Override
        public Set<String> on(APush line) {
            return new HashSet<String>(Arrays.asList(line.getReg()));
        }

        @Override
        public Set<String> on(APop line) { return new HashSet<String>(); }

        @Override
        public Set<String> on(AShift line) {
            return new HashSet<String>(Arrays.asList(line.getReg()));
        }

        @Override
        public Set<String> on(AString line) { return new HashSet<String>(); }

        @Override
        public Set<String> on(ATrivial line) { return new HashSet<String>(); }

        @Override
        public Set<String> on(AUnaryOp line) {
            return new HashSet<String>(Arrays.asList(line.getReg()));
        }

        @Override
        public Set<String> on(AWhitespace line) { return new HashSet<String>(); }
    }

    // change write behavior of accepting lines to write to given reg
    // returns false iff it fails, e.g. because the write can't be changed or was already deleted
    private static class ChangeWrites implements AssemblyLine.AssemblyLineVisitor<Boolean> {
        private String reg;

        public ChangeWrites(String reg) {
            this.reg = reg;
        }

        @Override
        public Boolean on(ACall line) {
            throw new RuntimeException("Makes no sense to change write destination.");
        }

        @Override
        public Boolean on(ACmov line) {
            line.setRight(reg);
            return true;
        }

        @Override
        public Boolean on(ACmp line) {
            throw new RuntimeException("Makes no sense to change write destination.");
        }

        @Override
        public Boolean on(ACommand line) {
            //throw new RuntimeException("Makes no sense to change write destination.");
            return false;
        }

        @Override
        public Boolean on(AJmp line) {
            throw new RuntimeException("Makes no sense to change write destination.");
        }

        @Override
        public Boolean on(ALabel line) {
            throw new RuntimeException("Makes no sense to change write destination.");
        }

        @Override
        public Boolean on(AMov line) {
            line.setRight(reg);
            return true;
        }

        @Override
        public Boolean on(AOps line) {
            line.setRight(reg);
            return true;
        }

        @Override
        public Boolean on(APush line) {
            throw new RuntimeException("Makes no sense to change write destination.");
        }

        @Override
        public Boolean on(APop line) {
            line.setReg(reg);
            return true;
        }

        @Override
        public Boolean on(AShift line) {
            return false;  // can't actually change this
        }

        @Override
        public Boolean on(AString line) {
            throw new RuntimeException("Makes no sense to change write destination.");
        }

        @Override
        public Boolean on(ATrivial line) {
            return false;
        }

        @Override
        public Boolean on(AUnaryOp line) {
            line.setReg(reg);
            return true;
        }

        @Override
        public Boolean on(AWhitespace line) {
            throw new RuntimeException("Makes no sense to change write destination.");
        }
    }


}

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
        codeList = simplifyPairMovs(codeList);
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

    // if I store something in %x, then don't touch it or %y before doing mov %x %y,
    // and overwrite it without doing anything else to it, eliminate it
    public static List<AssemblyLine> reduceMovs(List<AssemblyLine> l) {
        return l; // TODO
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
                        codeList.set(index, new ACommand("; formerly push " + regPush));
                        codeList.set(i, new AMov(regPush, regPop));
                    }
                    codeList.set(index, new ACommand("; formerly push " + regPush));
                    codeList.set(i, new ACommand("; formerly pop " + regPop));
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


}

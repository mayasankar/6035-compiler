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
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class CodeSimplifier {

    // return a simplified version of codeList
    public static List<AssemblyLine> simplifyMovs(List<AssemblyLine> codeList) {
        codeList = removeTrivialMovs(codeList);
        codeList = simplifyPairMovs(codeList);
        codeList = removeTrivialMovs(codeList);
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


}

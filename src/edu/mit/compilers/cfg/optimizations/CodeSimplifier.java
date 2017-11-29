package edu.mit.compilers.cfg.optimizations;

import java.util.Map;

import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.decl.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class CodeSimplifier {

    private static String concat(List<String> codeList) {
        String code = "";
        for (String s : codeList) {
            code += s + "\n";
        }
        return code;
    }

    // return a simplified version of codeList
    public static String simplifyMovs(String code) {
        List<String> codeList = Arrays.asList(code.split("\\n"));
        codeList = removeTrivialMovs(codeList);
        codeList = simplifyPairMovs(codeList);
        codeList = removeTrivialMovs(codeList);
        return concat(codeList);
    }

    public static List<String> removeTrivialMovs(List<String> l) {
        List<String> codeList = new ArrayList<>(l);
        Pattern pattern = Pattern.compile("mov (.+?), (.+)");

        for (int i=0; i<codeList.size(); i++) {
            Matcher matcher = pattern.matcher(codeList.get(i));
            if (matcher.find()) {
                String reg1 = matcher.group(1);
                String reg2 = matcher.group(2);
                if (reg1.equals(reg2)) {
                    // useless mov from a thing to itself; delete the line
                    codeList.set(i, "");
                }
            }
        }
        return codeList;
    }

    public static List<String> simplifyPairMovs(List<String> l) {
        List<String> codeList = new ArrayList<>(l);
        Pattern pattern = Pattern.compile("mov (.+?), (.+)");

        String lastReg1 = "";
        String lastReg2 = "";
        String reg1 = "";
        String reg2 = "";

        for (int i=0; i<codeList.size(); i++) {
            Matcher matcher = pattern.matcher(codeList.get(i));
            if (matcher.find()) {
                reg1 = matcher.group(1);
                reg2 = matcher.group(2);
                // if we had a move on the last line, and the register
                // moved into is the same as the first one we move now,
                // and not both others were on the stack, simplify
                if (!lastReg1.equals("") && lastReg2.equals(reg1) && (lastReg1.startsWith("%") || reg2.startsWith("%"))) {
                    // useless mov from a thing to itself; delete the line
                    codeList.set(i-1, "");
                    codeList.set(i, "mov " + lastReg1 + ", " + reg2);
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

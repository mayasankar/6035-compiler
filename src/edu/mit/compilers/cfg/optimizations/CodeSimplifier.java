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
        return String.join("", codeList);
    }

    // return a simplified version of codeList
    public static String simplifyMovs(String code) {
        List<String> codeList = Arrays.asList(code.split("\\n"));
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
        return concat(codeList);
    }

}

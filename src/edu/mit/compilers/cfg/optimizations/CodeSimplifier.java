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

    // return a simplified version of codeList
    public static String simplifyMovs(String code) {
        List<String> codeList = Arrays.asList(code.split("\\n"));
        Pattern pattern = Pattern.compile("mov (.+?), (.+)");

        for (int i=0; i<10; i++) {
            Matcher matcher = pattern.matcher(codeList.get(i));
            if (matcher.find()) {
                String reg1 = matcher.group(1);
                String reg2 = matcher.group(2);
                System.out.println(codeList.get(i));
                System.out.println("Regs: " + reg1 + ", " + reg2 + "\n");
            }
        }
        return codeList.get(0); // TODO
    }

}

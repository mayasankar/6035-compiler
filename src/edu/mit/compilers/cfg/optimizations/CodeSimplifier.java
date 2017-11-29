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

public class CodeSimplifier {

    // return a simplified version of codeList
    public static String simplifyMovs(String code) {
        List<String> codeList = Arrays.asList(code.split("\\n"));
        for (int i=0; i<10; i++) {
            System.out.println(codeList.get(i));
        }
        return codeList.get(0); // TODO
    }

}

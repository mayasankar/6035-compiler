package edu.mit.compilers.cfg;

import java.util.HashMap;
import java.util.Map;

import edu.mit.compilers.symbol_tables.MethodTable;

public class CFGProgram {
    private final Map<String, CFG> methodCFGMap = new HashMap<>();
    private final MethodTable methodTable;
   
    public void addMethod(String name, CFG methodCFG) {
        methodCFGMap.put(name, methodCFG);
    }
}

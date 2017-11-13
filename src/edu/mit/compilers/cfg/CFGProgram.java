package edu.mit.compilers.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.mit.compilers.ir.IRProgram;
import edu.mit.compilers.symbol_tables.MethodTable;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.cfg.lines.*;

public class CFGProgram {
    private final Map<String, CFG> methodCFGMap = new HashMap<>();
    private final MethodTable methodTable;

    public CFGProgram(IRProgram program) {
    	methodTable = program.getMethodTable();
    }

    public void addMethod(String name, CFG methodCFG) {
        methodCFGMap.put(name, methodCFG);
    }

    public Set<String> getMethodNames() {
    	return methodCFGMap.keySet();
    }

    public CFG getMethodCFG(String methodName) {
    	if(methodCFGMap.containsKey(methodName)) {
    		return methodCFGMap.get(methodName);
    	}

    	throw new RuntimeException("Method with name " + methodName +" not found in the program.");
    }

    public TypeDescriptor getMethodReturnType(String methodName) {
    	return methodTable.get(methodName).getReturnType();
    }

    public void blockify() {
    	for(String name: methodCFGMap.keySet()) {
    		methodCFGMap.put(name, methodCFGMap.get(name).blockify());
    	}
    }

    public int getNumParams(String method) {
        return methodTable.get(method).getParameters().getVariableList().size();
    }
}

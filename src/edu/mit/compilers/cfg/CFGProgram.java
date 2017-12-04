package edu.mit.compilers.cfg;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import edu.mit.compilers.ir.IRProgram;
import edu.mit.compilers.symbol_tables.MethodTable;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.cfg.optimizations.MethodDescriptor;

public class CFGProgram {
    private final Map<String, CFG> methodCFGMap = new HashMap<>();
    private final MethodTable methodTable;
    private final List<VariableDescriptor> globalVariables = new ArrayList<>();
    private final Map<String, List<VariableDescriptor>> localVariables = new HashMap<>();
    private Map<String, MethodDescriptor> methodDescriptors;

    public CFGProgram(IRProgram program) {
    	methodTable = program.getMethodTable();
    }

    public void recalculateMethodDescriptors() { methodDescriptors = MethodDescriptor.calculateMethodDescriptors(this); }

    public void addMethod(String name, CFG methodCFG) {
        methodCFGMap.put(name, methodCFG);
    }

    public void addGlobalVariable(VariableDescriptor global) {
        globalVariables.add(global);
    }

    public void addLocalVariable(String methodName, VariableDescriptor local) {
        if(localVariables.containsKey(methodName)) {
        	localVariables.get(methodName).add(local);
        } else {
        	List<VariableDescriptor> list = new ArrayList<>();
        	list.add(local);
        	localVariables.put(methodName, list);
        }
    }

    public List<VariableDescriptor> getGlobalVariables() {
        return globalVariables;
    }

    public Set<String> getGlobalNames() {
        Set<String> answer = new HashSet<>();
        for (VariableDescriptor var : globalVariables) {
            answer.add(var.getName());
        }
        return answer;
    }

    public List<String> getMethodNames() {
        List<String> names = new ArrayList<>();
        for (IRMethodDecl decl : methodTable.getMethodList()) {
            names.add(decl.getName());
        }
        return names;
    }

    public Map<String, MethodDescriptor> getMethodDescriptors() { return methodDescriptors; }

    public List<VariableDescriptor> getLocalVariablesForMethod(String method) {
		if(localVariables.containsKey(method)) {
        	return localVariables.get(method);
    	} else {
			return new ArrayList<VariableDescriptor>();
		}
	}

    public Map<String, CFG> getMethodToCFGMap() {
        return methodCFGMap;
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
    	for(String name : methodCFGMap.keySet()) {
            methodCFGMap.get(name).blockify();
    		methodCFGMap.put(name, methodCFGMap.get(name));
    	}
    }

    public int getNumParams(String method) {
        return methodTable.get(method).getParameters().getVariableList().size();
    }

    public List<IRMemberDecl> getAllParameters(String method) {
        return methodTable.get(method).getParameters().getVariableList();
    }

    public IRMethodDecl getMethodParameters(String methodName) {
    	return methodTable.get(methodName);
    }
}

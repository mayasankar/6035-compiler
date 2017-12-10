package edu.mit.compilers.assembly;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import edu.mit.compilers.cfg.CFG;
import edu.mit.compilers.cfg.CFGLocationAssigner;
import edu.mit.compilers.cfg.CFGProgram;
import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.assembly.lines.*;
import edu.mit.compilers.cfg.optimizations.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;

public class VariableStackAssigner implements CFGLocationAssigner {
	private Map<String, VariableDescriptor> variables = new HashMap<>();
	private Map<String, VariableDescriptor> globals = new HashMap<>();

	public VariableStackAssigner(CFGProgram program) {
		for (VariableDescriptor var : program.getGlobalVariables()) {
			globals.put(var.getName(), var);
		}
		Map<String, CFG> methodMap = program.getMethodToCFGMap();
		for (String method : methodMap.keySet()) {
			int stackPointer = 0;
			for(VariableDescriptor desc: program.getLocalVariablesForMethod(method)) {
				stackPointer = desc.pushOntoStack(stackPointer);
				variables.put(desc.getName(), desc);
			}
			List<IRMemberDecl> params = program.getAllParameters(method);
			for (IRMemberDecl param : params) {
				VariableDescriptor newDescriptor = new VariableDescriptor(param.getName());
				stackPointer = newDescriptor.pushOntoStack(stackPointer);
				variables.put(param.getName(), newDescriptor);
			}
		}
	}

	@Override
	public VariableDescriptor getVar(String variableName) {
		VariableDescriptor var = variables.get(variableName);
		if (var == null) {
			var = globals.get(variableName);
		}
		if (var == null) {
			throw new RuntimeException("Attempted to access unallocated variable '" + variableName + "'.");
		}
		return var;
	}



	@Override
    public List<AssemblyLine> pullInArguments(IRMethodDecl decl) {
        List<AssemblyLine> lines = new ArrayList<>();
        List<IRMemberDecl> parameters = decl.getParameters().getVariableList();
        for(int i=0; i < parameters.size(); ++i) {
            IRMemberDecl param = parameters.get(i);
            String paramLoc = getParamLoc(i);

            if (i<=5) {
                lines.addAll(moveToStore(param.getName(), paramLoc, "%r10", null));
            } else {
                lines.add(new AMov(paramLoc, "%r11"));
                lines.addAll(moveToStore(param.getName(), "%r11", "%r10", null));
            }
        }
        return lines;
    }

    private String getParamLoc(int i) {
        if(i==0) {
            return "%rdi";
        } else if (i==1) {
            return "%rsi";
        } else if (i==2) {
            return "%rdx";
        } else if (i==3) {
            return "%rcx";
        } else if (i==4) {
            return "%r8";
        } else if (i==5) {
            return "%r9";
        } else {
            return (i-4)*8 + "(%rbp)";
        }
     }


	@Override
    public boolean isVarStoredInRegister(String variable, CFGLine line){
	    return false;
	}

	@Override
    public boolean isFreeRegister(String register, CFGLine line){
		return true;
	}

	@Override
    public String getLocationOfVariable(String variable, CFGLine line){
		if(variable.startsWith("$")) {return variable;}
		VariableDescriptor var = getVar(variable);

	    return "-" + var.getStackOffset() + "(%rbp)";
	}

	@Override
    public String getFirstFreeRegister(CFGLine line){
		return "%r10";
	}

    @Override
    public int getNumAllocs(){
		int allocs = 0;
		for(VariableDescriptor desc: variables.values()) {
			allocs += desc.getSpaceRequired();
		}
		return allocs;
	}

    @Override
    public String getSecondFreeRegister(CFGLine line) {
        return "%r11";
    }

    @Override
    public boolean isGlobal(String variableName) {
        return globals.containsKey(variableName);
    }
}

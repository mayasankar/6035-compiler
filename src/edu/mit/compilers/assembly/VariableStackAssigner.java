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

	private VariableDescriptor getVar(String variableName) {
		VariableDescriptor var = variables.get(variableName);
		if (var == null) {
			var = globals.get(variableName);
		}
		if (var == null) {
			throw new RuntimeException("Attempted to access unallocated variable '" + variableName + "'.");
		}
		return var;
	}

	private String getAddress(String variableName) {
		VariableDescriptor var = getVar(variableName);
		int offset = var.getStackOffset();
		if (! variables.containsKey(variableName)) {
			// it's a global variable
			return "$" + variableName;
		}
		if (var.isArray()) {
            return "-" + (new Integer(offset).toString()) + "(%rbp, %r10, 8)";
        } else {
            return "-" + (new Integer(offset).toString()) + "(%rbp)";
        }
	}

	// move variable to targetRegister from stack
	// usually both registers should be %r10; don't use %r11; if not an array nor global, indexRegister doesn't matter
	public List<AssemblyLine> moveFromStore(String variableName, String targetRegister, String indexRegister) {
		VariableDescriptor var = getVar(variableName);
		int offset = var.getStackOffset();
		List<AssemblyLine> lines = new ArrayList<>();
		if (! variables.containsKey(variableName)) {
			// it's a global variable
			if (var.isArray()) {
				lines.add(new APush("%r11"));
				lines.add(new AMov("$3", "%rcx"));  // shift to multiply by 8
                lines.add(new AShift("shl", indexRegister));
				lines.add(new AMov("$" + variableName, "%r11"));
				lines.add(new AOps("add", "%r11", indexRegister));
				lines.add(new AMov("0(" + indexRegister + ")", "%r11"));
				lines.add(new AMov("%r11", targetRegister));
				lines.add(new APop("%r11"));
				return lines;
			}
			else {
				lines.add(new APush("%r11"));
				lines.add(new AMov("$" + variableName, indexRegister));
				lines.add(new AMov("0(" + indexRegister + ")", "%r11")); // TODO can we remove the %r11 (and thus the pushes) to simplify this?
				lines.add(new AMov("%r11", targetRegister));
				lines.add(new APop("%r11"));
				return lines;
			}
		}
		String strLoc = "-" + new Integer(offset).toString();
		if (var.isArray()) {
			strLoc += "(%rbp, " + indexRegister + ", 8)";
		} else {
			strLoc += "(%rbp)";
        }
		lines.add(new AMov(strLoc, targetRegister));
		return lines;
	}

	// move variable from sourceRegister to the stack
	// usually should be %r11 and %r10; source and index should not be same register and index shouldn't be %r11; if not an array nor global, indexRegister doesn't matter
	public List<AssemblyLine> moveToStore(String variableName, String sourceRegister, String indexRegister) {
		VariableDescriptor var = getVar(variableName);
		int offset = var.getStackOffset();
		List<AssemblyLine> lines = new ArrayList<>();
		if (! variables.containsKey(variableName)) {
			// it's a global variable
			if (var.isArray()) {
				lines.add(new APush("%r11"));
				lines.add(new AMov("$3", "%rcx"));  // shift to multiply by 8
                lines.add(new AShift("shl", indexRegister));
				lines.add(new AMov("$" + variableName, "%r11"));
				lines.add(new AOps("add", "%r11", indexRegister));
				lines.add(new APop("%r11"));
				lines.add(new AMov(sourceRegister, "0(" + indexRegister + ")"));
				return lines;
			}
			else {
				lines.add(new AMov("$" + variableName, indexRegister));
				lines.add(new AMov(sourceRegister, "0(" + indexRegister + ")"));
				return lines;
			}
		}
		String strLoc = "-" + new Integer(offset).toString();
		if (var.isArray()) {
			strLoc += "(%rbp, " + indexRegister + ", 8)";
		} else {
			strLoc += "(%rbp)";
		}
		lines.add(new AMov(sourceRegister, strLoc));
		return lines;
	}

	public String getMaxSize(String variableName) {
		VariableDescriptor var = getVar(variableName);
		if (var.isArray()) {
			int max_index = var.getLength();
			return "$" + (new Integer(max_index).toString());
        } else {
            throw new RuntimeException("Attempted to call getMaxSize() on non-array variable '" + variableName + "'.");
        }
	}


	@Override
    public List<AssemblyLine> pullInArguments(IRMethodDecl decl) {
        List<AssemblyLine> lines = new ArrayList<>();
        List<IRMemberDecl> parameters = decl.getParameters().getVariableList();
        for(int i=0; i < parameters.size(); ++i) {
            IRMemberDecl param = parameters.get(i);
            String paramLoc = getParamLoc(i);

            if (i<=5) {
                lines.addAll(moveToStore(param.getName(), paramLoc, "%r10"));
            } else {
                lines.add(new AMov(paramLoc, "%r11"));
                lines.addAll(moveToStore(param.getName(), "%r11", "%r10"));
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
		VariableDescriptor var = getVar(variable);

	    return "-" + var.getSpaceRequired() + "(%rbp)";
	}

	@Override
    public String getFreeRegister(CFGLine line){
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
    public String getIndexRegister(CFGLine line) {
        return "%r11";
    }
}

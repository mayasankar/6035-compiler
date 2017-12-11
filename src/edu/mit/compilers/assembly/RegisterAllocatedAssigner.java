package edu.mit.compilers.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.mit.compilers.assembly.lines.AMov;
import edu.mit.compilers.assembly.lines.AOps;
import edu.mit.compilers.assembly.lines.AssemblyLine;
import edu.mit.compilers.cfg.CFG;
import edu.mit.compilers.cfg.CFGLocationAssigner;
import edu.mit.compilers.cfg.CFGProgram;
import edu.mit.compilers.cfg.lines.CFGLine;
import edu.mit.compilers.ir.decl.IRMemberDecl;
import edu.mit.compilers.ir.decl.IRMethodDecl;
import edu.mit.compilers.symbol_tables.VariableDescriptor;

public class RegisterAllocatedAssigner implements CFGLocationAssigner {
    private static final String[] REGISTERS_FOR_USE = {"%r9", "%r8", "%rcx", "%rdx", "%rsi", "%rdi"};

    Map<String, VariableDescriptor> globals = new HashMap<>();
    Map<String, VariableDescriptor> variables = new HashMap<>();
    Set<String> usedRegisters = new HashSet<>();
    int numAllocs;

    public RegisterAllocatedAssigner(CFGProgram program, Map<String, Integer> colors) {
        for (VariableDescriptor var : program.getGlobalVariables()) {
            globals.put(var.getName(), var);
        }
        Map<String, CFG> methodMap = program.getMethodToCFGMap();
        for (String method : methodMap.keySet()) {
            int stackPointer = 0;
            for (VariableDescriptor desc: program.getLocalVariablesForMethod(method)) {
                if (colors.get(desc.getName()) == null) {
                    throw new RuntimeException("No color for " + desc.getName() + " in colors: " + colors.toString());
                }
                if (colors.get(desc.getName()) < REGISTERS_FOR_USE.length) {
                    desc.putInRegister(REGISTERS_FOR_USE[colors.get(desc.getName())]);
                    usedRegisters.add(REGISTERS_FOR_USE[colors.get(desc.getName())]);
                } else {
                    stackPointer = desc.pushOntoStack(stackPointer);
                }
                variables.put(desc.getName(), desc);
            }
            List<IRMemberDecl> params = program.getAllParameters(method);
            for (IRMemberDecl param : params) {
                VariableDescriptor desc = new VariableDescriptor(param.getName());
                if (colors.get(desc.getName()) == null) {
                    throw new RuntimeException("No color for " + desc.getName() + " in colors: " + colors.toString());
                }
                if (colors.get(desc.getName()) < REGISTERS_FOR_USE.length) {
                    desc.putInRegister(REGISTERS_FOR_USE[colors.get(desc.getName())]);
                    usedRegisters.add(REGISTERS_FOR_USE[colors.get(desc.getName())]);
                } else {
                    stackPointer = desc.pushOntoStack(stackPointer);
                }
                variables.put(desc.getName(), desc);
            }
            numAllocs = stackPointer;
        }

    }

    private VariableDescriptor getVar(String variable) {
        if (globals.containsKey(variable)) {
            return globals.get(variable);
        } else if (variables.containsKey(variable)) {
            return variables.get(variable);
        } else {
            throw new RuntimeException("variable " + variable + " not found in CFG");
        }
    }

    @Override
    public List<AssemblyLine> pullInArguments(IRMethodDecl decl) {
        List<String> paramRegs = Arrays.asList("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9");
        List<AssemblyLine> lines = new ArrayList<>();
        List<IRMemberDecl> parameters = decl.getParameters().getVariableList();
        for(int i=0; i < parameters.size(); ++i) {
            int paramInTempStore = -1;
            IRMemberDecl param = parameters.get(i);
            String paramLoc = getParamLoc(i);
            if (i == paramInTempStore) {
                paramLoc = "%r11";
            }
            if (i<=5) {
                VariableDescriptor var = getVar(param.getName());
                if( var.inRegister() && paramRegs.indexOf(var.getRegister())>i) {
                    lines.add(new AMov(var.getRegister(), "%r11"));
                    paramInTempStore = paramRegs.indexOf(var.getRegister());
                }
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
    public boolean isVarStoredInRegister(String variable, CFGLine line) {
        if (variable.startsWith("$")) {
            return true;
        }
        return getVar(variable).inRegister();
    }

    @Override
    public boolean isFreeRegister(String register, CFGLine line) {
        return usedRegisters.contains(register);
    }

    @Override
    public String getLocationOfVariable(String variable, CFGLine line) {
        if(variable.startsWith("$")) {
            return variable;
        } else if(globals.containsKey(variable)) {
            return "$" + variable;
        } else {
            VariableDescriptor var = getVar(variable);
            if(var.inRegister()) {
                return var.getRegister();
            } else {
                return "-" + var.getStackOffset() + "(%rbp)";
            }
        }
    }

    @Override
    public String getFirstFreeRegister(CFGLine line) {
        return "%r10";
    }

    @Override
    public String getSecondFreeRegister(CFGLine line) {
        return "%r11";
    }

    @Override
    public List<AssemblyLine> moveFromStore(String variableName, String targetRegister, String indexRegister) {
        List<AssemblyLine> lines = new ArrayList<>();
        if(variableName.startsWith("$")) {
            lines.add(new AMov(variableName, targetRegister));
            return lines;
        }
        VariableDescriptor var = getVar(variableName);
        if (var.inRegister()) {
            lines.add(new AMov(var.getRegister(), targetRegister));
            return lines;
        }
        int offset = var.getStackOffset();
        if (! variables.containsKey(variableName)) {
            // it's a global variable
            if (var.isArray()) {
                lines.add(new AOps("imul", "$8", indexRegister));
                lines.add(new AOps("add", "$" + variableName, indexRegister));
                lines.add(new AMov("0(" + indexRegister + ")", targetRegister));
                return lines;
            }
            else {
                lines.add(new AMov("$" + variableName, indexRegister));
                lines.add(new AMov("0(" + indexRegister + ")", targetRegister)); // TODO can we remove the %r11 (and thus the pushes) to simplify this?
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

    @Override
    public List<AssemblyLine> moveToStore(String variableName, String sourceRegister, String indexRegister) {
        VariableDescriptor var = getVar(variableName);
        List<AssemblyLine> lines = new ArrayList<>();

        if(var.inRegister()) {
            lines.add(new AMov(sourceRegister, var.getRegister()));
            return lines;
        }
        int offset = var.getStackOffset();
        if (! variables.containsKey(variableName)) {
            // it's a global variable
            if (var.isArray()) {
                lines.add(new AOps("imul", "$8", indexRegister));
                lines.add(new AOps("add", "$" + variableName, indexRegister));
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

    @Override
    public String getMaxSize(String variable) {
        VariableDescriptor var = getVar(variable);
        if (var.isArray()) {
            int max_index = var.getLength();
            return "$" + (new Integer(max_index).toString());
        } else {
            throw new RuntimeException("Attempted to call getMaxSize() on non-array variable '" + variable + "'.");
        }
    }

    @Override
    public int getNumAllocs() {
        return numAllocs;
    }
}

package edu.mit.compilers.cfg;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.assembly.lines.*;
import edu.mit.compilers.cfg.lines.CFGLine;
import edu.mit.compilers.ir.decl.IRMethodDecl;
import edu.mit.compilers.symbol_tables.VariableDescriptor;


public interface CFGLocationAssigner {
    public List<AssemblyLine> pullInArguments(IRMethodDecl decl);

    public boolean isVarStoredInRegister(String variable, CFGLine line);

    public boolean isFreeRegister(String register, CFGLine line);

    public String getLocationOfVariable(String variable, CFGLine line);

    public String getFirstFreeRegister(CFGLine line);
    public String getSecondFreeRegister(CFGLine line);

    public int getNumAllocs();
    
    public VariableDescriptor getVar(String variableName);
    
    public boolean isGlobal(String variableName);
    
    // ---------------------------- Implemented Methods Below ----------------------------------------------
    
    public default String getMaxSize(String variableName) {
        VariableDescriptor var = getVar(variableName);
        if (var.isArray()) {
            int max_index = var.getLength();
            return "$" + (new Integer(max_index).toString());
        } else {
            throw new RuntimeException("Attempted to call getMaxSize() on non-array variable '" + variableName + "'.");
        }
    }
    
    public default List<AssemblyLine> moveFromStore(String variable, String targetRegister, String indexRegister, CFGLine line) {
        List<AssemblyLine> lines = new ArrayList<AssemblyLine>();
        if(isVarStoredInRegister(variable, line)) {
            String reg = getLocationOfVariable(variable, line);
            lines.add(new AMov(reg, targetRegister));
        } else {
            lines = moveFromNonRegister(variable, targetRegister, indexRegister);
        }
        
        return lines;
    }
    
    public default List<AssemblyLine> moveToStore(String variable, String locRegister, String indexRegister, CFGLine line) {
        List<AssemblyLine> lines = new ArrayList<AssemblyLine>();
        if(isVarStoredInRegister(variable, line)) {
            String reg = getLocationOfVariable(variable, line);
            lines.add(new AMov(locRegister, reg));
        } else {
            lines = moveFromNonRegister(variable, locRegister, indexRegister);
        }
        
        return lines;
    }
    
    default List<AssemblyLine> moveFromNonRegister(String variableName, String targetRegister, String indexRegister) {
        List<AssemblyLine> lines = new ArrayList<>();
        if(variableName.startsWith("$")) { 
            lines.add(new AMov(variableName, targetRegister));
            return lines;
        }
        VariableDescriptor var = getVar(variableName);
        if (isGlobal(variableName)) {
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
        
        String strLoc = "-" + var.getStackOffset();
        if (var.isArray()) {
            strLoc += "(%rbp, " + indexRegister + ", 8)";
        } else {
            strLoc += "(%rbp)";
        }
        lines.add(new AMov(strLoc, targetRegister));
        return lines;
    }
    
    default List<AssemblyLine> moveToNonRegister(String variableName, String sourceRegister, String indexRegister) {
        VariableDescriptor var = getVar(variableName);
        List<AssemblyLine> lines = new ArrayList<>();
        if (isGlobal(variableName)) {
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
        
        String strLoc = "-" + var.getStackOffset();
        if (var.isArray()) {
            strLoc += "(%rbp, " + indexRegister + ", 8)";
        } else {
            strLoc += "(%rbp)";
        }
        lines.add(new AMov(sourceRegister, strLoc));
        return lines;
    }
}

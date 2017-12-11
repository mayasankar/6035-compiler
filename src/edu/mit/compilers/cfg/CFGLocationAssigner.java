package edu.mit.compilers.cfg;

import java.util.List;

import edu.mit.compilers.assembly.lines.AssemblyLine;
import edu.mit.compilers.cfg.lines.CFGLine;
import edu.mit.compilers.ir.decl.IRMethodDecl;


public interface CFGLocationAssigner {
    public List<AssemblyLine> pullInArguments(IRMethodDecl decl);

    public boolean isVarStoredInRegister(String variable, CFGLine line);

    public boolean isFreeRegister(String register, CFGLine line);

    public String getLocationOfVariable(String variable, CFGLine line);

    public String getFirstFreeRegister(CFGLine line);
    public String getSecondFreeRegister(CFGLine line);

    public List<AssemblyLine> moveFromStore(String variable, String targetRegister, String indexRegister);
    public List<AssemblyLine> moveToStore(String variable, String locRegister, String indexRegister);

    public String getMaxSize(String variable);

    public int getNumAllocs();
}

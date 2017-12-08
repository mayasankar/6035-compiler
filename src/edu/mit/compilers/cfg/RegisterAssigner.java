package edu.mit.compilers.cfg;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import edu.mit.compilers.assembly.lines.AssemblyLine;
import edu.mit.compilers.cfg.lines.CFGLine;
import edu.mit.compilers.ir.decl.IRMethodDecl;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.cfg.optimizations.*;

public class RegisterAssigner implements CFGLocationAssigner {
    private ConflictGraph cg;
    private static List<String> availableRegisters = new ArrayList<>(Arrays.asList("%r8", "%r9", "%r10", "%r11", "%rax", "%rcx", "%rdx", "%rsi", "%rdi"));
    private int numAllocs = 0;
    // could also add callee-saved and push/pop at start of function: "%rbx", "%r12", "%r13", "%r14", "%r15"
    private Map<String, String> registerAssignments;


    public RegisterAssigner(ConflictGraph cg) {  // TODO pass it something else?
        this.cg = cg;
        assignRegisters();
        registerAssignments = this.assignRegisters();
    }

    // return a mapping from variable to register; register is null if no register to give it
    private Map<String, String> assignRegisters() {
        Map<String, Integer> integerColoring = this.cg.colorGraph();
        Map<String, String> registerColoring = new HashMap<>();
        for (String variable : integerColoring.keySet()) {
            Integer colorNum = integerColoring.get(variable);
            String register = "";
            if (colorNum < this.availableRegisters.size()) {
                register = this.availableRegisters.get(colorNum);
            }
            else {
                this.numAllocs += 1;
                register = "-" + (new Integer(8*this.numAllocs).toString()) + "(%rbp)";
            }
            registerColoring.put(variable, register);
        }
        return registerColoring;
    }

    @Override
    public List<AssemblyLine> pullInArguments(IRMethodDecl decl) { throw new RuntimeException("Unimplemented."); }  // TODO

    @Override
    public boolean isVarStoredInRegister(String variable, CFGLine line) {
        // TODO should this involve line?
        return registerAssignments.get(variable).startsWith("%");
    }
    @Override
    public boolean isExpressionStoredInRegister(IRExpression expr, CFGLine line) { throw new RuntimeException("Unimplemented."); }  // TODO

    @Override
    public boolean isFreeRegister(String register, CFGLine line) { throw new RuntimeException("Unimplemented."); }  // TODO

    @Override
    public String getLocationOfVariable(String variable, CFGLine line) { throw new RuntimeException("Unimplemented."); }  // TODO
    @Override
    public String getLocationOfVarExpression(IRExpression variable, CFGLine line) { throw new RuntimeException("Unimplemented."); }  // TODO

    @Override
    public String getFreeRegister(CFGLine line) { throw new RuntimeException("Unimplemented."); }  // TODO

    @Override
    public List<AssemblyLine> pullFromStack(String variable, String targetRegister, String indexRegister) { throw new RuntimeException("Unimplemented."); }  // TODO
    @Override
    public List<AssemblyLine> pushToStack(String variable, String locRegister, String indexRegister) { throw new RuntimeException("Unimplemented."); }  // TODO

    @Override
    public String getMaxSize(String variable) { throw new RuntimeException("Unimplemented."); }  // TODO

    @Override
    public int getNumAllocs() { return this.numAllocs; }
}

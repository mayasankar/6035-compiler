package edu.mit.compilers.cfg;

import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class MethodAssembler implements CFGLine.CFGNewVisitor<String> {

    private String label;
    private int numParams;
    private VariableStackAssigner stacker;
    private TypeDescriptor returnType;
    
    public MethodAssembler(String method, int numParams, VariableStackAssigner stacker, TypeDescriptor returnType) {
        this.label = method;
        this.numParams = numParams;
        this.stacker = stacker;
        this.returnType = returnType;
    }
    
    public String assemble(CFG cfg) {
        return cfg.getStart().accept(this);
    }

    @Override
    public String on(CFGAssignStatement2 line) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String on(CFGConditional line) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String on(CFGNoOp line) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String on(CFGReturn line) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String on(CFGMethodCall line) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String on(CFGBlock block) {
        // TODO Auto-generated method stub
        return null;
    }

}

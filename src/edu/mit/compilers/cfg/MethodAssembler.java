package edu.mit.compilers.cfg;

import java.util.Map;

import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class MethodAssembler implements CFGLine.CFGVisitor<String> {

    private String label;
    private int numAllocs;
    private VariableStackAssigner stacker;
    private TypeDescriptor returnType;
    
    private Map<CFGBlock, String> blockNames;
    private int blockCount = 0;
    
    private Map<String, String> stringNames;
    private int stringCount = 0;
    
    
    public MethodAssembler(String method, int numParams, VariableStackAssigner stacker, TypeDescriptor returnType) {
        this.label = method;
        this.numAllocs = numParams;
        this.stacker = stacker;
        this.returnType = returnType;
    }
    
    public String assemble(CFG cfg) {
        return cfg.getStart().accept(this);
    }

    @Override
    public String on(CFGAssignStatement line) {
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
        if(blockNames.containsKey(block)) {
            return "";
        } else {
            blockNames.put(block, label + "_" + blockCount);
            blockCount += 1;
        }
        String code = "";
        for(CFGLine line: block.getLines()) {
            code += line.accept(this);
        }
        return code + block.getTrueBranch().accept(this) + block.getFalseBranch().accept(this);
    }


}

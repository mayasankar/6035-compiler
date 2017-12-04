package edu.mit.compilers.cfg.optimizations;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.math.BigInteger;
import java.util.Arrays;

import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;
import edu.mit.compilers.cfg.lines.*;

public class CfgAssignVisitor implements CFGLine.CFGVisitor<Set<String>> {
    private final boolean includeArrays;
    private final boolean returnNestedGlobals;
    private final Map<String, MethodDescriptor> methodDescriptors;

    public CfgAssignVisitor() {
        this(true);
    }

    public CfgAssignVisitor(boolean includeArrays) {
        this(includeArrays, null);
    }

    public CfgAssignVisitor(Map<String, MethodDescriptor> mds) {
        this(true, mds);
    }

    private CfgAssignVisitor(boolean includeArrays, Map<String, MethodDescriptor> mds) {
        this.includeArrays = includeArrays;
        this.returnNestedGlobals = (mds != null);
        this.methodDescriptors = mds;
        if (! includeArrays && returnNestedGlobals) {
            throw new RuntimeException("CfgAssignVisitor not implemented for these inputs");
        }
    }

	@Override
    public Set<String> on(CFGBlock line){
        // TODO should this do anything other than nothing? I think we never call it on this
        //throw new RuntimeException("CfgAssignVisitor should never be called on a CFGBlock.");
        Set<String> assignSet = new HashSet<>();
        for (CFGLine l : line.getLines()) {
            assignSet.addAll(l.accept(this));
        }
        return assignSet;
    }


    @Override
    public Set<String> on(CFGAssignStatement line){
        if (!includeArrays && line.getVarAssigned().isArray()) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(line.getVarAssigned().getName()));
    }

    @Override
    public Set<String> on(CFGBoundsCheck line){
        return new HashSet<>();
    }

    @Override
    public Set<String> on(CFGConditional line){
        return new HashSet<>();
    }

    @Override
    public Set<String> on(CFGMethodCall line){
        Set<String> answer = new HashSet<>();
        if (returnNestedGlobals) {
            answer.addAll(methodDescriptors.get(line.getExpression().getName())
                                           .getGlobalsAssigned(includeArrays));
        }
        return answer;
    }

    @Override
    public Set<String> on(CFGReturn line){
        return new HashSet<>();
    }

    @Override
    public Set<String> on(CFGNoOp line){
        return new HashSet<>();
    }
}

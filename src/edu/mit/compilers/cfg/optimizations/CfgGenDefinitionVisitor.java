package edu.mit.compilers.cfg.optimizations;

import java.util.List;
import java.util.HashSet;
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

public class CfgGenDefinitionVisitor implements CFGLine.CFGVisitor<Set<CP.CPDefinition>> {
    private USEVisitor use;

    public CfgGenDefinitionVisitor(Map<String, MethodDescriptor> mds) {
        use = new USEVisitor(mds);
    }

	@Override
    public Set<CP.CPDefinition> on(CFGBlock line){
        throw new RuntimeException("This would be annoying and currently unnecessary to implement.");
    }

    @Override
    public Set<CP.CPDefinition> on(CFGAssignStatement line){
        Set<CP.CPDefinition> answer = new HashSet<>();
        answer.add(new CP.CPDefinition(line.getVarAssigned(), line.getExpression(), use));
		return answer;
    }

    @Override
    public Set<CP.CPDefinition> on(CFGConditional line){
        return new HashSet<>();
    }

    @Override
    public Set<CP.CPDefinition> on(CFGMethodCall line){
        return new HashSet<>();
    }

    @Override
    public Set<CP.CPDefinition> on(CFGNoOp line){
        return new HashSet<>();
    }


    @Override
    public Set<CP.CPDefinition> on(CFGNoReturnError line){
        return new HashSet<>();
    }

    @Override
    public Set<CP.CPDefinition> on(CFGReturn line){
        return new HashSet<>();
    }

    @Override
    public Set<CP.CPDefinition> on(CFGBoundsCheck line){
        return new HashSet<>();
    }
}

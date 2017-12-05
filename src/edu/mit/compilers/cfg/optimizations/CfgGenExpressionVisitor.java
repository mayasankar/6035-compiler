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

public class CfgGenExpressionVisitor implements CFGLine.CFGVisitor<Map<IRExpression, Set<String>>> {

	@Override
    public Map<IRExpression, Set<String>> on(CFGBlock line){
        throw new RuntimeException("This would be annoying and currently unnecessary to implement.");
    }

    @Override
    public Map<IRExpression, Set<String>> on(CFGAssignStatement line){
        Map<IRExpression, Set<String>> retMap = new HashMap<>();
		retMap.put(line.getExpression(), new HashSet<String>(Arrays.asList(line.getVarAssigned().getName())));
		return retMap;
    }

	@Override
	public Map<IRExpression, Set<String>> on(CFGBoundsCheck line){
		return new HashMap<>();
	}

    @Override
    public Map<IRExpression, Set<String>> on(CFGConditional line){
        return new HashMap<>();
    }

    @Override
    public Map<IRExpression, Set<String>> on(CFGMethodCall line){
        return new HashMap<>();
    }

    @Override
    public Map<IRExpression, Set<String>> on(CFGReturn line){
        return new HashMap<>();
    }

    @Override
    public Map<IRExpression, Set<String>> on(CFGNoOp line){
        return new HashMap<>();
    }

    @Override
    public Map<IRExpression, Set<String>> on(CFGNoReturnError line){
        return new HashMap<>();
    }
}

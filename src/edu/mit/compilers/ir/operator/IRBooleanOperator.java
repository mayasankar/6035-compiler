package edu.mit.compilers.ir.operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.mit.compilers.ir.IRType;

public class IRBooleanOperator extends IRBinaryOperator {
	
	protected IRBooleanOperator(String symbol) {
		super(symbol);
	}

	@Override
	public IRType outputType() {
		return IRType.boolType();
	}
	
	public static final IRBooleanOperator andOp = new IRBooleanOperator("&&");
	
	public static final IRBooleanOperator orOp = new IRBooleanOperator("||");
	
	public static final Map<String, IRBooleanOperator> booleanStringToOperatorMap;
	
	static {
		Map<String, IRBooleanOperator> map = new HashMap<>();
		for(IRBooleanOperator op: Arrays.asList(andOp, orOp)){
			map.put(op.toString(), op);
		}
		
		booleanStringToOperatorMap = Collections.unmodifiableMap(map);
	}
}

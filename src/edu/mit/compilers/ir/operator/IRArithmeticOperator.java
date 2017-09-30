package edu.mit.compilers.ir.operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.mit.compilers.ir.IRType;

public class IRArithmeticOperator extends IRBinaryOperator {
	
	protected IRArithmeticOperator(String symbol) {
		super(symbol);
	}

	@Override
	public IRType outputType() {
		return IRType.intType();
	}
	
	public static final IRArithmeticOperator addOp = new IRArithmeticOperator("+");
	
	public static final IRArithmeticOperator subOp = new IRArithmeticOperator("-");
	
	public static final IRArithmeticOperator mulOp = new IRArithmeticOperator("*");	
	
	public static final IRArithmeticOperator divOp = new IRArithmeticOperator("/");
	
	public static final IRArithmeticOperator remOp = new IRArithmeticOperator("%");
	
	public static final Map<String, IRArithmeticOperator> arithmeticStringToOperatorMap;
	
	static {
		Map<String, IRArithmeticOperator> map = new HashMap<>();
		for(IRArithmeticOperator op: Arrays.asList(addOp, subOp, mulOp, divOp, remOp)){
			map.put(op.toString(), op);
		}
		
		arithmeticStringToOperatorMap = Collections.unmodifiableMap(map);
		
	}
}

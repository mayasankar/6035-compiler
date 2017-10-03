package edu.mit.compilers.ir.operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.mit.compilers.ir.IRType;

public class IRBinaryArithmeticOperator extends IRBinaryOperator {
	
	protected IRBinaryArithmeticOperator(String symbol) {
		super(symbol);
	}

	@Override
	public IRType outputType() {
		return IRType.intType();
	}
	
	public static final IRBinaryArithmeticOperator addOp = new IRBinaryArithmeticOperator("+");
	
	public static final IRBinaryArithmeticOperator subOp = new IRBinaryArithmeticOperator("-");
	
	public static final IRBinaryArithmeticOperator mulOp = new IRBinaryArithmeticOperator("*");	
	
	public static final IRBinaryArithmeticOperator divOp = new IRBinaryArithmeticOperator("/");
	
	public static final IRBinaryArithmeticOperator remOp = new IRBinaryArithmeticOperator("%");
	
	public static final Map<String, IRBinaryArithmeticOperator> arithmeticStringToOperatorMap;
	
	static {
		Map<String, IRBinaryArithmeticOperator> map = new HashMap<>();
		for(IRBinaryArithmeticOperator op: Arrays.asList(addOp, subOp, mulOp, divOp, remOp)){
			map.put(op.toString(), op);
		}
		
		arithmeticStringToOperatorMap = Collections.unmodifiableMap(map);
		
	}
}

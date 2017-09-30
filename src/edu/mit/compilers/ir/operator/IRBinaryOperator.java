package edu.mit.compilers.ir.operator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class IRBinaryOperator extends IROperator {

	protected IRBinaryOperator(String symbol) {
		super(symbol);
	}
	
	public static final Map<String, IRBinaryOperator> stringToBinaryOperatorMap; 
	static {
		Map<String, IRBinaryOperator> map = new HashMap<>();
		
		map.putAll(IRArithmeticOperator.arithmeticStringToOperatorMap);
		map.putAll(IRBooleanOperator.booleanStringToOperatorMap);
		map.putAll(IREqualityOperator.equalityStringToOperatorMap);
		
		stringToBinaryOperatorMap = Collections.unmodifiableMap(map);
		
	}

}

package edu.mit.compilers.ir.operator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.mit.compilers.ir.IRType;

public abstract class IROperator {
	
	private final String symbol;
	
	protected IROperator(String symbol) {
		this.symbol = symbol;
	}
	
	/**
	 * 
	 * @return the resulting type of applying this expression to two valid arguments
	 */
	public abstract IRType outputType();
	
	@Override
	public String toString() {
		return symbol;
	}
	
	public static final Map<String, IROperator> stringToOperatorMap; 
	static {
		Map<String, IROperator> map = new HashMap<String, IROperator>();
		
		map.putAll(IRBinaryOperator.stringToBinaryOperatorMap);
		
		stringToOperatorMap = Collections.unmodifiableMap(map);
		
	}
}

package edu.mit.compilers.ir.operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.mit.compilers.ir.IRType;

public class IREqualityOperator extends IRBinaryOperator {
	
	protected IREqualityOperator(String symbol) {
		super(symbol);
	}

	@Override
	public IRType outputType() {
		return IRType.boolType();
	}
	
	public static final IREqualityOperator eqOp = new IREqualityOperator("==");
	
	public static final IREqualityOperator neqOp = new IREqualityOperator("!=");
	
	public static final IREqualityOperator ltOp = new IREqualityOperator("<");	
	
	public static final IREqualityOperator gtOp = new IREqualityOperator(">");
	
	public static final IREqualityOperator lteOp = new IREqualityOperator("<=");
	
	public static final IREqualityOperator gteOp = new IREqualityOperator(">=");
	
	public static final Map<String, IREqualityOperator> equalityStringToOperatorMap;
	
	static {
		Map<String, IREqualityOperator> map = new HashMap<>();
		for(IREqualityOperator op: Arrays.asList(eqOp, neqOp, ltOp, gtOp, lteOp, gteOp)){
			map.put(op.toString(), op);
		}
		
		equalityStringToOperatorMap = Collections.unmodifiableMap(map);
		
	}
}

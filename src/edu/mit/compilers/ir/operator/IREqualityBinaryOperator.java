package edu.mit.compilers.ir.operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.mit.compilers.ir.IRType;

public class IREqualityBinaryOperator extends IRBinaryOperator {

	protected IREqualityBinaryOperator(String symbol) {
		super(symbol);
	}

	@Override
	public IRType.Type outputType() {
		return IRType.Type.BOOL;
	}
	
	public static final IREqualityBinaryOperator eqOp = new IREqualityBinaryOperator("==");

	public static final IREqualityBinaryOperator neqOp = new IREqualityBinaryOperator("!=");

	public static final IREqualityBinaryOperator ltOp = new IREqualityBinaryOperator("<");

	public static final IREqualityBinaryOperator gtOp = new IREqualityBinaryOperator(">");

	public static final IREqualityBinaryOperator lteOp = new IREqualityBinaryOperator("<=");

	public static final IREqualityBinaryOperator gteOp = new IREqualityBinaryOperator(">=");

	public static final Map<String, IREqualityBinaryOperator> equalityStringToOperatorMap;

	static {
		Map<String, IREqualityBinaryOperator> map = new HashMap<>();
		for(IREqualityBinaryOperator op: Arrays.asList(eqOp, neqOp, ltOp, gtOp, lteOp, gteOp)){
			map.put(op.toString(), op);
		}

		equalityStringToOperatorMap = Collections.unmodifiableMap(map);

	}
}

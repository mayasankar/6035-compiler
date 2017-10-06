package edu.mit.compilers.ir.operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.mit.compilers.ir.IRType;

public class IRBooleanBinaryOperator extends IRBinaryOperator {

	protected IRBooleanBinaryOperator(String symbol) {
		super(symbol);
	}

	@Override
	public IRType.Type outputType() {
		return IRType.Type.BOOL;
	}

	public static final IRBooleanBinaryOperator andOp = new IRBooleanBinaryOperator("&&");

	public static final IRBooleanBinaryOperator orOp = new IRBooleanBinaryOperator("||");

	public static final Map<String, IRBooleanBinaryOperator> booleanStringToOperatorMap;

	static {
		Map<String, IRBooleanBinaryOperator> map = new HashMap<>();
		for(IRBooleanBinaryOperator op: Arrays.asList(andOp, orOp)){
			map.put(op.toString(), op);
		}

		booleanStringToOperatorMap = Collections.unmodifiableMap(map);
	}
}

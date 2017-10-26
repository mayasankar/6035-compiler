package edu.mit.compilers.ir.expression.literal;

import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.expression.IRExpression;

public class IRIntLiteral extends IRLiteral<BigInteger> {

	public IRIntLiteral(BigInteger value) {
		super(value);
		expressionType = IRExpression.ExpressionType.INT_LITERAL;
	}

	public void invert() {
		value = value.negate();
	}

	@Override
	public IRType.Type getType() {
		return IRType.Type.INT;
	}

	@Override
	public String toString() {
		return "" + value + "";
	}
}

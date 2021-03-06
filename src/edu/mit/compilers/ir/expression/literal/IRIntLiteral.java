package edu.mit.compilers.ir.expression.literal;

import java.math.BigInteger;

import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class IRIntLiteral extends IRLiteral<BigInteger> {
    public static final IRIntLiteral ZERO = new IRIntLiteral(BigInteger.ZERO);
    public static final IRIntLiteral ONE = new IRIntLiteral(BigInteger.ONE);

	public IRIntLiteral(BigInteger value) {
		super(value);
		expressionType = IRExpression.ExpressionType.INT_LITERAL;
	}

    public IRIntLiteral copy() {
        return new IRIntLiteral(this.value);
    }

	public void invert() {
		value = value.negate();
	}

	@Override
	public TypeDescriptor getType() {
		return TypeDescriptor.INT;
	}

	@Override
	public String toString() {
		return "" + value + "";
	}

	@Override
    public <R> R accept(IRNodeVisitor<R> visitor) {
        return visitor.onInt(this);
    }

	@Override
	public <R> R accept(IRExpressionVisitor<R> visitor) {
		return visitor.on(this);
	}
}

package edu.mit.compilers.ir.expression.literal;

import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class IRBoolLiteral extends IRLiteral<Boolean> {
    public static final IRBoolLiteral TRUE = new IRBoolLiteral(true);
    public static final IRBoolLiteral FALSE = new IRBoolLiteral(false);

	public IRBoolLiteral(Boolean value) {
		super(value);
		expressionType = IRExpression.ExpressionType.BOOL_LITERAL;
	}

    @Override
    public IRBoolLiteral copy() {
        return new IRBoolLiteral(this.value);
    }

	@Override
	public TypeDescriptor getType() {
		return TypeDescriptor.BOOL;
	}

    @Override
    public <R> R accept(IRNodeVisitor<R> visitor) {
        return visitor.onBool(this);
    }

	@Override
	public <R> R accept(IRExpressionVisitor<R> visitor) {
		return visitor.on(this);
	}
}

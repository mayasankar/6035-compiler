package edu.mit.compilers.ir.expression.literal;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.expression.IRExpression;

public class IRBoolLiteral extends IRLiteral<Boolean> {

	public IRBoolLiteral(Boolean value) {
		super(value);
		expressionType = IRExpression.ExpressionType.BOOL_LITERAL;
	}

	@Override
	public IRType.Type getType() {
		return IRType.Type.BOOL;
	}

    @Override
    public <R> R accept(IRNodeVisitor<R> visitor) {
        return visitor.onBool(this);
    }
}

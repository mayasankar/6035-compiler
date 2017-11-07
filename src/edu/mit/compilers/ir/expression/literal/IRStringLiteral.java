package edu.mit.compilers.ir.expression.literal;

import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class IRStringLiteral extends IRLiteral<String> {

	public IRStringLiteral(String value) {
		super(value);
		expressionType = IRExpression.ExpressionType.STRING_LITERAL;
	}

	@Override
	public TypeDescriptor getType() {
		return TypeDescriptor.STRING;
	}

	@Override
	public String toString() {
		return "\"" + value + "\"";
	}
	
	@Override
    public <R> R accept(IRNodeVisitor<R> visitor) {
        return visitor.onString(this);
    }
}

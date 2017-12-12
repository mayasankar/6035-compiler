package edu.mit.compilers.ir.expression;

import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.symbol_tables.TypeDescriptor;

public abstract class IRExpression extends IRNode {
	public abstract TypeDescriptor getType();

	public static enum ExpressionType {
		UNSPECIFIED,
		BOOL_LITERAL,
		INT_LITERAL,
		STRING_LITERAL,
		UNARY,
		BINARY,
		TERNARY,
		LEN,
		METHOD_CALL,
		VARIABLE,
	}

    public interface Renameable {
        public void resetName(String newName);
    }

	public interface IRExpressionVisitor<R>{
		public R on(IRUnaryOpExpression ir);
		public R on(IRBinaryOpExpression ir);
		public R on(IRTernaryOpExpression ir);
		public R on(IRLenExpression ir);
		public R on(IRVariableExpression ir);
		public R on(IRMethodCallExpression ir);
		public R on(IRBoolLiteral ir);
		public R on(IRIntLiteral ir);
		public R on(IRStringLiteral ir);
	}

	public abstract <R> R accept(IRExpressionVisitor<R> visitor);

	protected ExpressionType expressionType = ExpressionType.UNSPECIFIED;

	public ExpressionType getExpressionType() { return expressionType; }

	public abstract int getDepth();

	@Override
	public abstract List<IRExpression> getChildren();

    public abstract IRExpression copy();

    public boolean isConstant() {
        for (IRExpression child : getChildren()) {
            if (!(child.isConstant())) { return false; }
        }
        return true;
    }

    public boolean equalsExpression(IRExpression other) {
        if (other instanceof IRVariableExpression) { return false; }
        return this.equals(other);
    }

}

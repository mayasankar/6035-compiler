package edu.mit.compilers.ir.expression.literal;

import java.util.List;
import java.util.Arrays;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRNode.IRNodeVisitor;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRExpression.IRExpressionVisitor;

public abstract class IRLiteral<T> extends IRExpression {
	protected T value;

	public IRLiteral(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public List<IRExpression> getChildren() {
		return Arrays.asList();
	}
	
	@Override
	public int getDepth() {
		return 0;
	}
	
	@Override
	public <R> R accept(IRExpressionVisitor<R> visitor) {
		return visitor.on(this);
	}

	@Override
	public <R> R accept(IRNodeVisitor<R> visitor) {
		return visitor.on(this);
	}
}

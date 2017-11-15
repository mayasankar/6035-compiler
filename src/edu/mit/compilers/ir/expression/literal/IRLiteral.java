package edu.mit.compilers.ir.expression.literal;

import java.util.List;
import java.util.Arrays;

import edu.mit.compilers.ir.expression.IRExpression;

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
	public boolean equals(Object obj) {
		if (obj instanceof IRLiteral) {
			IRLiteral expr = (IRLiteral)obj;
			return (this.getType().equals(expr.getType()) && this.value.equals(expr.value));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.value.hashCode() + 17*this.getType().hashCode();
	}
}

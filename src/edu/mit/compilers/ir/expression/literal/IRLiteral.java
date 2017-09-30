package edu.mit.compilers.ir.expression.literal;

import edu.mit.compilers.ir.expression.IRExpression;

public abstract class IRLiteral<T> extends IRExpression {
	protected T value;
	
	public IRLiteral(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
}

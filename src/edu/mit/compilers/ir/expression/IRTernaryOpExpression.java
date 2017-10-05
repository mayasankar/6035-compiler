package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;

public class IRTernaryOpExpression extends IRExpression {

	private IRExpression condition;

	private IRExpression trueExpression;

	private IRExpression falseExpression;

	// public IRTernaryOpExpression(IRExpression condition, IRExpression trueExpression, IRExpression falseExpression) {
	// 	this.condition = condition;
	// 	this.falseExpression = falseExpression;
	// 	this.trueExpression = trueExpression;
	// }

	@Override
	public IRType getType() {
		return trueExpression.getType();
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(condition, trueExpression, falseExpression);
	}

}

package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.operator.IRUnaryOperator;

public class IRUnaryOpExpression extends IRExpression {

	private IRExpression argument;

	private IRUnaryOperator operator;

	// public IRUnaryOpExpression(IRExpression argument, IRUnaryOperator operator) {
	// 	this.argument = argument;
	// 	this.operator = operator;
	// }

	@Override
	public IRType getType() {
		return operator.outputType();
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(argument);
	}

}

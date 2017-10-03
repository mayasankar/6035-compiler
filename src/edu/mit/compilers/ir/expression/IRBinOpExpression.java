package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.operator.IRBinaryOperator;

public class IRBinOpExpression extends IRExpression{
	private final IRExpression leftExpr;
	
	private final IRExpression rightExpr;
	
	private final IRBinaryOperator operator;
	
	public IRBinOpExpression(IRExpression leftExpr, IRExpression rightExpr, IRBinaryOperator operator) {
		this.leftExpr = leftExpr;
		this.rightExpr = rightExpr;
		this.operator = operator;
	}
	
	@Override
	public IRType getType() {
		// TODO Where are we static checking types???
		return operator.outputType();
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(leftExpr, rightExpr);
	}

}

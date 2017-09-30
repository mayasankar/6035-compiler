package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.operator.IRBinaryOperator;

public class IRBinOpExpression extends IRExpression{
	private final IRNode leftExpr;
	
	private final IRNode rightExpr;
	
	private final IRBinaryOperator operator;
	
	public IRBinOpExpression(IRNode leftExpr, IRNode rightExpr, IRBinaryOperator operator) {
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
	public List<IRNode> getChildren() {
		return Arrays.asList(leftExpr, rightExpr);
	}

}

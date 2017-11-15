package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.trees.ConcreteTree;

public class IRBinaryOpExpression extends IRExpression{
	private IRExpression leftExpr;
	private IRExpression rightExpr;
	private String operator;

	public IRBinaryOpExpression(IRExpression leftExpr, Token operator, IRExpression rightExpr) {
		//setLineNumbers(leftExpr);
		expressionType = IRExpression.ExpressionType.BINARY;
		this.leftExpr = leftExpr;
		this.operator = operator.getText();
		this.rightExpr = rightExpr;
	}

	public IRBinaryOpExpression(IRExpression leftExpr, String operator, IRExpression rightExpr) {
		//setLineNumbers(leftExpr);
		expressionType = IRExpression.ExpressionType.BINARY;
		this.leftExpr = leftExpr;
		this.operator = operator;
		this.rightExpr = rightExpr;
	}

	public IRExpression getLeftExpr() { return leftExpr; }
	public IRExpression getRightExpr() { return rightExpr; }
	public String getOperator() { return operator; }

	private boolean isCommutative() {
		switch (operator) {
			case "==": case "!=": case "&&": case "||": case "+":case "*":
				return true;
			case "<": case "<=": case ">": case ">=": case "-": case "/": case "%":
				return false;
			default:
				throw new RuntimeException("Undefined operator " + operator + ".");
		}
	}

	@Override
	public TypeDescriptor getType() {
		switch (operator) {
			case "==": case "!=": case "&&": case "||": case "<": case "<=": case ">": case ">=":
				return TypeDescriptor.BOOL;
			case "+": case "-": case "*": case "/": case "%":
				return TypeDescriptor.INT;
			default:
				throw new RuntimeException("Undefined operator " + operator + ".");
		}
	}

	@Override
	public List<IRExpression> getChildren() {
		return Arrays.asList(leftExpr, rightExpr);
	}

	@Override
	public String toString() { //TODO remove if null
		return (leftExpr == null ? "var" : leftExpr.toString()) + " " + operator + " " + (rightExpr == null ? "var" : rightExpr.toString());
	}

	@Override
	public int getDepth() {
		return Math.max(leftExpr.getDepth(), rightExpr.getDepth()) + 1;
	}

	@Override
	public <R> R accept(IRExpressionVisitor<R> visitor) {
		return visitor.on(this);
	}

	@Override
	public <R> R accept(IRNodeVisitor<R> visitor) {
		return visitor.on(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IRBinaryOpExpression) {
			IRBinaryOpExpression expr = (IRBinaryOpExpression)obj;
			if (! this.operator.equals(expr.operator)) {
				return false;
			}
			if (this.isCommutative()) {
				if (this.leftExpr.equals(expr.rightExpr) && this.rightExpr.equals(expr.leftExpr)) {
					return true;
				}
			}
			return (this.leftExpr.equals(expr.leftExpr) && this.rightExpr.equals(expr.rightExpr));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.rightExpr.hashCode() + this.leftExpr.hashCode() + 17*this.operator.hashCode();
	}

}

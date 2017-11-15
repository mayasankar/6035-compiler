package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.IRNode.IRNodeVisitor;
import edu.mit.compilers.ir.expression.IRExpression.IRExpressionVisitor;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.trees.ConcreteTree;

public class IRTernaryOpExpression extends IRExpression {

	private IRExpression condition;

	private IRExpression trueExpression;

	private IRExpression falseExpression;

	public IRTernaryOpExpression(IRExpression condition, IRExpression trueExpression, IRExpression falseExpression) {
		expressionType = IRExpression.ExpressionType.TERNARY;
		setLineNumbers(condition);
		this.condition = condition;
		this.trueExpression = trueExpression;
		this.falseExpression = falseExpression;
	}

	public IRExpression getCondition() { return condition; }
	public IRExpression getTrueExpression() { return trueExpression; }
	public IRExpression getFalseExpression() { return falseExpression; }

	@Override
	public TypeDescriptor getType() {
		return trueExpression.getType();
	}

	@Override
	public List<IRExpression> getChildren() {
		return Arrays.asList(condition, trueExpression, falseExpression);
	}

	@Override
	public String toString() {
		return condition + " ? " + trueExpression + " : " + falseExpression;
	}

	@Override
	public int getDepth() {
		return Math.max(Math.max(trueExpression.getDepth(), falseExpression.getDepth()), condition.getDepth()) + 1;
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
		if (obj instanceof IRTernaryOpExpression) {
			IRTernaryOpExpression expr = (IRTernaryOpExpression)obj;
			return (this.condition.equals(expr.condition) && this.trueExpression.equals(expr.trueExpression) && this.falseExpression.equals(expr.falseExpression));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.condition.hashCode() + 17*this.trueExpression.hashCode() + 19*this.falseExpression.hashCode();
	}

}

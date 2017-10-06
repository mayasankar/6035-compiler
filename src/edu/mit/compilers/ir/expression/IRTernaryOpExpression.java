package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.trees.ConcreteTree;

public class IRTernaryOpExpression extends IRExpression {

	private IRExpression condition;

	private IRExpression trueExpression;

	private IRExpression falseExpression;

	public IRTernaryOpExpression(IRExpression condition, IRExpression trueExpression, IRExpression falseExpression) {
		this.condition = condition;
		this.trueExpression = trueExpression;
		this.falseExpression = falseExpression;
	}

	public IRTernaryOpExpression(ConcreteTree tree) {
		setLineNumbers(tree);
		ConcreteTree child = tree.getFirstChild();
		condition = IRExpression.makeIRExpression(child);
		child = child.getRightSibling();
		trueExpression = IRExpression.makeIRExpression(child);
		child = child.getRightSibling();
		falseExpression = IRExpression.makeIRExpression(child);
	}

	public IRExpression getCondition() { return condition; }
	public IRExpression getTrueExpression() { return trueExpression; }
	public IRExpression getFalseExpression() { return falseExpression; }

	@Override
	public IRType.Type getType() {
		return trueExpression.getType();
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(condition, trueExpression, falseExpression);
	}

  // TODO uncomment when expressions cannot be null.
	// @Override
	// public String toString() {
	// 	return condition + " ? " + trueExpression + " : " + falseExpression;
	// }

}

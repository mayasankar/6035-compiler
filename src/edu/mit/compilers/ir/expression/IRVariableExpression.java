package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.trees.ConcreteTree;

public class IRVariableExpression extends IRExpression {

	private String variableName;
	private IRExpression arrayIndex = null;
	private IRType.Type type = null;

	public static IRVariableExpression makeIRVariableExpression(ConcreteTree tree) { // TODO make constructor
		if (tree == null) {
			System.err.println("ERROR: null tree in IRVariableExpression.makeIRVariableExpression.");
		}
		ConcreteTree child = tree.getFirstChild();
		String name = child.getToken().getText();
		child = child.getRightSibling();
		IRVariableExpression toReturn;
		if (child == null) {
			toReturn = new IRVariableExpression(name);
		} else {
			child = child.getRightSibling();
			toReturn = new IRVariableExpression(name, makeIRExpression(child));
		}
		toReturn.expressionType = IRExpression.ExpressionType.VARIABLE;
		toReturn.setLineNumbers(tree);
		return toReturn;
	}

	public IRVariableExpression(Token id) {
		expressionType = IRExpression.ExpressionType.VARIABLE;
		variableName = id.getText();
	}

	public IRVariableExpression(String variableName) {
		expressionType = IRExpression.ExpressionType.VARIABLE;
		this.variableName = variableName;
		// TODO: make a variable or look one up here
	}

	public IRVariableExpression(String variableName, IRExpression expression) {
		expressionType = IRExpression.ExpressionType.VARIABLE;
		this.variableName = variableName;
		arrayIndex = expression;
	}

	public String getName() { return variableName; }
	public IRExpression getIndexExpression() { return arrayIndex; }

	@Override
	public IRType.Type getType() {
		return type;
	}

	public void setType(IRType.Type t) {
		type = t;
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList();
	}

	@Override
	public String toString() {
		if (arrayIndex == null) {
			return variableName;
		} else {
			return variableName + '[' + arrayIndex + ']';
		}
	}

}

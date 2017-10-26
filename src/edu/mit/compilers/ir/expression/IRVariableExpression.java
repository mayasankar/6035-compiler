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
	public List<IRExpression> getChildren() {
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

	@Override
	public int getDepth() {
		return 0;
	}

}

package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.trees.ConcreteTree;

public class IRVariableExpression extends IRExpression {

	private String variableName; // TODO remove eventually
	private IRExpression arrayIndex = null; // TODO remove eventually

	public static IRVariableExpression makeIRVariableExpression(ConcreteTree tree) { // TODO make constructor
		if (tree == null) {
			System.out.println("ERROR: null tree in IRVariableExpression.makeIRVariableExpression.");
		}
		ConcreteTree child = tree.getFirstChild();
		String name = child.getToken().getText();
		child = child.getRightSibling();
		if (child == null) {
			return new IRVariableExpression(name);
		} else {
			child = child.getRightSibling();
			return new IRVariableExpression(name, makeIRExpression(child));
		}
	}

	public IRVariableExpression(Token id) {
		variableName = id.getText();
	}

	public IRVariableExpression(String variableName) {
		this.variableName = variableName;
		// TODO: make a variable or look one up here
	}

	public IRVariableExpression(String variableName, IRExpression expression) {
		this.variableName = variableName;
		arrayIndex = expression;
	}

	@Override
	public IRType.Type getType() {
		return  null;//IRType.getTypeFromDescriptor(variable.getType()); TODO: This will complain until Variables have the right type
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

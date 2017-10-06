package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.symbol_tables.Variable;
import edu.mit.compilers.trees.ConcreteTree;

public class IRVariableExpression extends IRExpression {

	private Variable variable;
	private String variableName; // TODO remove eventually
	private IRExpression arrayIndex = null; // TODO remove eventually

	public static IRVariableExpression makeIRVariableExpression(ConcreteTree tree) { // TODO make constructor
		System.out.println("w1");
		ConcreteTree child = tree.getFirstChild();
		System.out.println("w1.5");
		String name = child.getToken().getText();
		System.out.println("w2");
		child = child.getRightSibling();
		System.out.println("w3");
		if (child == null) {
			System.out.println("w4");
			return new IRVariableExpression(name);
		} else {
			System.out.println("w5");
			child = child.getRightSibling();
			return new IRVariableExpression(name, makeIRExpression(child));
		}
	}

	private IRVariableExpression(String variableName) {
		this.variableName = variableName;
		// TODO: make a variable or look one up here
	}

	private IRVariableExpression(String variableName, IRExpression expression) {
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

package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.operator.IRUnaryOperator;
import edu.mit.compilers.trees.ConcreteTree;

public class IRUnaryOpExpression extends IRExpression {

	//private IRUnaryOperator operator;
	private Token operator;
	private IRExpression argument;

	public IRUnaryOpExpression(Token operator, IRExpression argument) {
		this.operator = operator;
		this.argument = argument;
	}

	public IRUnaryOpExpression(ConcreteTree tree) {
		setLineNumbers(tree);
		ConcreteTree exprChild = tree.getLastChild();
		ConcreteTree opChild = exprChild.getLeftSibling();
		ConcreteTree firstOpChild = tree.getFirstChild();
		IRExpression expression = IRExpression.makeIRExpression(exprChild);
		while (opChild != firstOpChild) {
			expression = new IRUnaryOpExpression(opChild.getToken(), expression);
			opChild = opChild.getLeftSibling();
		}
		this.operator = opChild.getToken();
		this.argument = expression;
	}

	public Token getOperator() { return operator; }
	public IRExpression getArgument() { return argument; }

	@Override
	public IRType.Type getType() {
		return null;
		//return operator.outputType();
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(argument);
	}

	@Override
	public String toString() { //TODO remove if null
		return operator.getText() + ((argument == null) ? "var" : argument);
	}

}

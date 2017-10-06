package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;
import edu.mit.compilers.trees.ConcreteTree;

public class IRAssignStatement extends IRStatement {

	private IRVariableExpression varAssigned;
	private Token operator;
	private IRExpression value;

	public IRAssignStatement(ConcreteTree tree) {
		System.out.println("v1");
		statementType = IRStatement.StatementType.ASSIGN_EXPR;
		ConcreteTree child = tree.getFirstChild();
		System.out.println("v2");
		varAssigned = IRVariableExpression.makeIRVariableExpression(child);
		child = child.getRightSibling();
		System.out.println("v3");
		operator = child.getToken();
		child = child.getRightSibling();
		System.out.println("v4");
		value = IRExpression.makeIRExpression(child);
	}

	public static IRAssignStatement makeForLoopInitializer(ConcreteTree firstToken) {
		//TODO fix
		return new IRAssignStatement(firstToken);
	}

	public static IRAssignStatement makeForLoopStepFunction(ConcreteTree firstToken) {
		//TODO fix
		return new IRAssignStatement(firstToken);
	}

	//@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(varAssigned, value);
	}

	@Override
	public String toString() {
		return varAssigned + " " + operator.getText() + " " + value;
	}

}

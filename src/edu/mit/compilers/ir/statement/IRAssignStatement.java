package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;
import edu.mit.compilers.trees.ConcreteTree;

public class IRAssignStatement extends IRStatement {

	private IRVariableExpression varAssigned;
	private Token operator;
	private IRExpression value; // NOTE null if operator is ++ or --

	public IRAssignStatement(ConcreteTree tree) {
		System.out.println("v1" + tree.getName());
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

	private IRAssignStatement(IRVariableExpression varAssigned, Token operator, IRExpression value) {
		this.varAssigned = varAssigned;
		this.operator = operator;
		this.value = value;
	}

	public static IRAssignStatement makeForLoopInitializer(ConcreteTree child) {
		//TODO test
		IRVariableExpression varAssigned = IRVariableExpression.makeIRVariableExpression(child);
		child = child.getRightSibling();
		Token operator = child.getToken();
		child = child.getRightSibling();
		IRExpression value = IRExpression.makeIRExpression(child);
		return new IRAssignStatement(varAssigned, operator, value);
	}

	public static IRAssignStatement makeForLoopStepFunction(ConcreteTree child) {
		//TODO test
		IRVariableExpression varAssigned = IRVariableExpression.makeIRVariableExpression(child);
		child = child.getRightSibling();
		Token operator = child.getToken();
		if (operator.getType() == DecafParserTokenTypes.OP_INC || operator.getType() == DecafParserTokenTypes.OP_DEC) {
			return new IRAssignStatement(varAssigned, operator, null);
		}
		child = child.getRightSibling();
		return new IRAssignStatement(varAssigned, operator, IRExpression.makeIRExpression(child));
	}

	//@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(varAssigned, value);
	}

	@Override
	public String toString() {
		return varAssigned + " " + operator.getText() + (value == null ? "" : " " + value);
	}

}

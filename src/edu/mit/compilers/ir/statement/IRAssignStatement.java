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
		statementType = IRStatement.StatementType.ASSIGN_EXPR;
		ConcreteTree child = tree.getFirstChild();
		if (child == null) {
			System.out.println("ERROR: null child tree in IRAssignStatement.IRAssignStatement.");
		}
		varAssigned = IRVariableExpression.makeIRVariableExpression(child);
		child = child.getRightSibling();
		operator = child.getToken();
		child = child.getRightSibling();
		value = IRExpression.makeIRExpression(child);
	}

	private IRAssignStatement(IRVariableExpression varAssigned, Token operator, IRExpression value) {
		this.varAssigned = varAssigned;
		this.operator = operator;
		this.value = value;
	}

	public String getVariableName(){
		return varAssigned.getVariableName();
	}
	public IRExpression getValue() { return value; }

	public static IRAssignStatement makeForLoopInitializer(ConcreteTree child) {
		IRVariableExpression varAssigned = new IRVariableExpression(child.getToken());
		child = child.getRightSibling();
		Token operator = child.getToken();
		child = child.getRightSibling();
		IRExpression value = IRExpression.makeIRExpression(child);
		IRAssignStatement toReturn = new IRAssignStatement(varAssigned, operator, value);
		toReturn.setLineNumbers(child);
		return toReturn;
	}

	public static IRAssignStatement makeForLoopStepFunction(ConcreteTree child) {
		IRVariableExpression varAssigned = IRVariableExpression.makeIRVariableExpression(child);
		child = child.getRightSibling();
		Token operator = child.getToken();
		IRAssignStatement toReturn;
		if (operator.getType() == DecafParserTokenTypes.OP_INC || operator.getType() == DecafParserTokenTypes.OP_DEC) {
			toReturn = new IRAssignStatement(varAssigned, operator, null);
		} else {
			child = child.getRightSibling();
			toReturn = new IRAssignStatement(varAssigned, operator, IRExpression.makeIRExpression(child));
		}
		toReturn.setLineNumbers(child);
		return toReturn;
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

package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;
import edu.mit.compilers.trees.ConcreteTree;

public class IRAssignStatement extends IRStatement {

	private IRVariableExpression varAssigned;
	// TODO need a token as to what operator is used -- compound or simple
	private IRExpression value;

	public IRAssignStatement(ConcreteTree tree) {
		statementType = IRStatement.StatementType.ASSIGN_EXPR;
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

}

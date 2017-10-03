package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;

public class IRAssignStatement extends IRStatement {

	private IRVariableExpression varAssigned;
	
	private IRExpression value;
	
	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(varAssigned, value);
	}

}

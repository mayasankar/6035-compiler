package edu.mit.compilers.cfg;

import antlr.Token;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;
import edu.mit.compilers.ir.statement.IRAssignStatement;

public class CFGAssignStatement extends CFGStatement {
	private String variableName;
	private Token op;
	private IRExpression value;
	
	public CFGAssignStatement(String variableName, Token op, IRExpression value) {
		super(new IRAssignStatement(new IRVariableExpression(variableName), op, value));
		this.variableName = variableName;
		this.op = op;
		this.value = value;
	}
}

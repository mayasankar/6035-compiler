package edu.mit.compilers.cfg;

import antlr.Token;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;
import edu.mit.compilers.ir.statement.IRAssignStatement;
import java.util.BitSet;

public class CFGAssignStatement extends CFGStatement {
	private String variableName; // NOTE -- do not use, it doesn't know if it is the name of the variable or the array you're indexing
	private Token op;
	private IRExpression value;

	public CFGAssignStatement(String variableName, Token op, IRExpression value) {
		super(new IRAssignStatement(new IRVariableExpression(variableName), op, value));
		this.variableName = variableName;
		this.op = op;
		this.value = value;
	}

	public CFGAssignStatement(String variableName, String indexLocation, Token op, IRExpression value) {
		super(new IRAssignStatement(new IRVariableExpression(variableName, new IRVariableExpression(indexLocation)), op, value));
		this.variableName = variableName;
		this.op = op;
		this.value = value;
	}

	@Override
    public <R> R accept(CFGBitSetVisitor<R> visitor, BitSet parentBitVector){
		return visitor.on(this, parentBitVector);
	}
}

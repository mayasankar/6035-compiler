package edu.mit.compilers.cfg.lines;

import antlr.Token;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;
import edu.mit.compilers.ir.statement.IRAssignStatement;
import java.util.Set;

public class CFGAssignStatement extends CFGLine {
	private IRVariableExpression varAssigned; // operation must be = here
    private IRExpression expression;

	public CFGAssignStatement(IRAssignStatement s) {
		if (!s.getOperator().equals("=")) {
			throw new RuntimeException("CFGAssignStatements must not have operators other than '=': " + s.getOperator());
		}
		this.varAssigned = s.getVarAssigned();
		if (this.varAssigned.getDepth() > 1) {
            throw new RuntimeException("CFGAssignStatements must not have >1 varAssigned depth: " + this.varAssigned.toString());
        }
		this.expression = s.getValue();
		if (this.expression.getDepth() > 1) {
            throw new RuntimeException("CFGAssignStatements must not have >1 expression depth: " + expression.toString());
        }
	}

    public CFGAssignStatement(IRVariableExpression varAssigned, IRExpression expression) {
        this.varAssigned = varAssigned;
        if (this.varAssigned.getDepth() > 1) {
            throw new RuntimeException("CFGAssignStatements must not have >1 varAssigned depth: " + this.varAssigned.toString());
        }
        this.expression = expression;
        if (this.expression.getDepth() > 1) {
            throw new RuntimeException("CFGAssignStatements must not have >1 expression depth: " + expression.toString());
        }
    }

	public CFGAssignStatement(String variableName, IRExpression expression) {
        this.varAssigned = new IRVariableExpression(variableName);
        if (expression.getDepth() > 1) {
            throw new RuntimeException("CFGAssignStatements must not have >1 expression depth: " + expression.toString());
        }
		this.expression = expression;
	}

	public CFGAssignStatement(String variableName, IRExpression indexLocation, IRExpression expression) {
        if (indexLocation.getDepth() > 0) {
            throw new RuntimeException("CFGAssignStatements must not have >0 indexLocation depth: " + indexLocation.toString());
        }
        this.varAssigned = new IRVariableExpression(variableName, indexLocation);
        if (expression.getDepth() > 1) {
            throw new RuntimeException("CFGAssignStatements must not have >1 expression depth: " + expression.toString());
        }
        this.expression = expression;
	}

    private CFGAssignStatement(CFGAssignStatement other) {
        this.varAssigned = other.varAssigned;
        this.expression = other.expression;
    }

    public IRVariableExpression getVarAssigned() { return this.varAssigned; }
    public IRExpression getExpression() { return this.expression; }
	public void setExpression(IRExpression expr) {
		if (expr.getDepth() > 1) {
            throw new RuntimeException("CFGAssignStatements must not have >1 expression depth: " + expression.toString());
        }
		this.expression = expr;
	}

    @Override
    public boolean isNoOp() { return false; }

    @Override
    public boolean isAssign() { return true; }

    @Override
    public CFGLine copy() { return new CFGAssignStatement(this); }

    @Override
    public String ownValue() {
        return this.varAssigned.toString() + " = " + this.expression.toString();
    }

	@Override
    public <R> R accept(CFGVisitor<R> visitor){
		return visitor.on(this);
	}
}

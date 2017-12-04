package edu.mit.compilers.cfg.lines;

import antlr.Token;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;


public class CFGBoundsCheck extends CFGLine {
	private IRVariableExpression arrayVariable;

	public CFGBoundsCheck(IRVariableExpression v) {
        if (!v.isArray()) {
            throw new RuntimeException("CFGBoundsChecks must have array variables.");
        }
		if (v.getDepth() > 1) {
            throw new RuntimeException("CFGBoundsChecks must not have >1 variable depth: " + this.arrayVariable.toString());
        }
        this.arrayVariable = v;
	}

	public CFGBoundsCheck(String variableName, IRExpression indexExpression) {
        this.arrayVariable = new IRVariableExpression(variableName, indexExpression);
        if (this.arrayVariable.getDepth() > 1) {
            throw new RuntimeException("CFGBoundsChecks must not have >1 variable depth: " + this.arrayVariable.toString());
        }
	}

    public IRVariableExpression getExpression() { return this.arrayVariable; }
    public IRExpression getIndexExpression() { return arrayVariable.getIndexExpression(); }
    public void setIndexExpression(IRExpression expr) { arrayVariable.setIndexExpression(expr); }

    @Override
    public boolean isNoOp() { return false; }

    @Override
    public boolean isAssign() { return false; }

    @Override
    public String ownValue() {
        return "Bounds check on " + this.arrayVariable.toString();
    }

	@Override
    public <R> R accept(CFGVisitor<R> visitor){
		return visitor.on(this);
	}
}

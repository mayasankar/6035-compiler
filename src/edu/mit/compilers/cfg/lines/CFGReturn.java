package edu.mit.compilers.cfg.lines;


import edu.mit.compilers.ir.expression.IRExpression;
import java.util.Set;

public class CFGReturn extends CFGLine {
    IRExpression expression; // null if void return statement

    public CFGReturn(CFGLine trueBranch, CFGLine falseBranch, IRExpression expression) {
        super(trueBranch, falseBranch);
        if (expression.getDepth() > 0) {
            throw new RuntimeException("CFGReturns must not have >0 expression depth: " + expression.toString());
        }
        this.expression = expression;
    }

    public CFGReturn(IRExpression expression) {
        super();
        if (expression.getDepth() > 0) {
            throw new RuntimeException("CFGReturns must not have >0 expression depth: " + expression.toString());
        }
        this.expression = expression;
    }

    public CFGReturn() {
        super();
        this.expression = null;
    }

    public IRExpression getExpression() {
        if (expression == null) {
            throw new RuntimeException("Trying to access expression of void return statement. Check isVoid() before calling getExpression() to fix.");
        }
        return expression;
    }
    public void setExpression(IRExpression expression) { this.expression = expression; }

    public boolean isVoid() { return expression == null; }

    @Override
    public boolean isNoOp() { return false; }
    @Override
    public boolean isAssign() { return false; }

    @Override
    public String ownValue() {
        if (expression == null) {
            return "";
        }
        return expression.toString();
    }

    @Override
    public <R> R accept(CFGVisitor<R> visitor){
		return visitor.on(this);
	}
}

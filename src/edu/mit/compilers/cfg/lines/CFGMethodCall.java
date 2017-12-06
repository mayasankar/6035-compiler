package edu.mit.compilers.cfg.lines;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import edu.mit.compilers.ir.expression.*;

public class CFGMethodCall extends CFGLine {
    IRMethodCallExpression expression;

    public CFGMethodCall(CFGLine trueBranch, CFGLine falseBranch, IRMethodCallExpression expression) {
        super(trueBranch, falseBranch);
        if (expression.getDepth() > 1) {
            throw new RuntimeException("CFGMethodCalls must not have >1 expression depth: " + expression.toString());
        }
        this.expression = expression;
    }

    public CFGMethodCall(IRMethodCallExpression expression) {
        super();
        if (expression.getDepth() > 1) {
            throw new RuntimeException("CFGMethodCalls must not have >1 expression depth: " + expression.toString());
        }
        this.expression = expression;
    }

    private CFGMethodCall(CFGMethodCall other) {
        this.expression = other.expression.copy();
    }

    public IRMethodCallExpression getExpression() { return expression; }
    public void setExpression(IRMethodCallExpression expression) { this.expression = expression; }

    @Override
    public boolean isNoOp() { return false; }
    @Override
    public boolean isAssign() { return false; }

    @Override
    public CFGMethodCall copy() { return new CFGMethodCall(this); }

    @Override
    public List<IRMethodCallExpression> getExpressions() { return Arrays.asList(expression); }

    @Override
    public String ownValue() {
        return expression.toString();
    }

    @Override
    public <R> R accept(CFGVisitor<R> visitor){
		return visitor.on(this);
	}
}

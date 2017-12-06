package edu.mit.compilers.cfg.lines;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import edu.mit.compilers.ir.expression.IRExpression;

public class CFGConditional extends CFGLine {
    IRExpression expression;

    public CFGConditional(CFGLine trueBranch, CFGLine falseBranch, IRExpression expression) {
        super(trueBranch, falseBranch);
        if (expression.getDepth() > 0) {
            throw new RuntimeException("CFGConditionals must not have >0 expression depth: " + expression.toString());
        }
        this.expression = expression;
    }

    public CFGConditional(IRExpression expression) {
        super();
        if (expression.getDepth() > 0) {
            throw new RuntimeException("CFGConditionals must not have >0 expression depth: " + expression.toString());
        }
        this.expression = expression;
    }

    private CFGConditional(CFGConditional other) {
        this.expression = other.expression.copy();
    }

    @Override
    public void setBranches(CFGLine trueBranch, CFGLine falseBranch) {
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
        trueBranch.addParentLine(this);
        if (falseBranch != trueBranch) { falseBranch.addParentLine(this); }
    }

    public IRExpression getExpression() { return expression; }
    public void setExpression(IRExpression expression) { this.expression = expression; }

    @Override
    public boolean isNoOp() { return false; }
    @Override
    public boolean isAssign() { return false; }

    @Override
    public CFGConditional copy() { return new CFGConditional(this); }

    @Override
    public List<IRExpression> getExpressions() { return Arrays.asList(expression); }

    @Override
    public String ownValue() {
        return expression.toString();
    }

    @Override
    public <R> R accept(CFGVisitor<R> visitor){
		return visitor.on(this);
	}
}

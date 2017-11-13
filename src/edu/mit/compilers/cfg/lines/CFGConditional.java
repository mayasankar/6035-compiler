package edu.mit.compilers.cfg.lines;


import edu.mit.compilers.ir.expression.IRExpression;
import java.util.Set;

public class CFGConditional extends CFGLine {
    IRExpression expression;

    public CFGConditional(CFGLine trueBranch, CFGLine falseBranch, IRExpression expression) {
        super(trueBranch, falseBranch);
        if (expression.getDepth() > 1) {
            throw new RuntimeException("CFGConditionals must not have >1 expression depth: " + expression.toString());
        }
        this.expression = expression;
    }

    public CFGConditional(IRExpression expression) {
        super();
        if (expression.getDepth() > 1) {
            throw new RuntimeException("CFGConditionals must not have >1 expression depth: " + expression.toString());
        }
        this.expression = expression;
    }

    public IRExpression getExpression() { return expression; }

    @Override
    public boolean isNoOp() { return false; }
    @Override
    public boolean isAssign() { return false; }

    @Override
    public String ownValue() {
        return expression.toString();
    }

    @Override
    public <R> R accept(CFGVisitor<R> visitor){
		return visitor.on(this);
	}
}

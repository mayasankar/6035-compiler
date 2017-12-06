package edu.mit.compilers.cfg.lines;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import edu.mit.compilers.ir.expression.IRExpression;

public class CFGNoOp extends CFGLine {

    public CFGNoOp() {
        super();
    }

    public CFGNoOp(CFGLine trueBranch, CFGLine falseBranch) {
        super(trueBranch, falseBranch);
    }

    @Override
    public boolean isNoOp() { return true; }
    @Override
    public boolean isAssign() { return false; }

    @Override
    public CFGNoOp copy() { return new CFGNoOp(); }
    @Override
    public List<IRExpression> getExpressions() { return Arrays.asList(); }

    @Override
    public String ownValue() {
        return "NOOP";
    }

    @Override
    public <R> R accept(CFGVisitor<R> visitor){
		return visitor.on(this);
	}
}

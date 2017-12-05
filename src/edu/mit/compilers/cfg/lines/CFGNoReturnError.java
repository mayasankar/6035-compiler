package edu.mit.compilers.cfg.lines;

import antlr.Token;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;


public class CFGNoReturnError extends CFGLine {

	public CFGNoReturnError() {}

    @Override
    public boolean isNoOp() { return false; }

    @Override
    public boolean isAssign() { return false; }

    @Override
    public String ownValue() {
        return "Control falls off not void method";
    }

	@Override
    public <R> R accept(CFGVisitor<R> visitor){
		return visitor.on(this);
	}
}

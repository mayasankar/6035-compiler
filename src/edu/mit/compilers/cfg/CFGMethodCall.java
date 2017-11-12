package edu.mit.compilers.cfg;
import edu.mit.compilers.cfg.CFGLine;
import edu.mit.compilers.ir.expression.*;
import java.util.Set;

public class CFGMethodCall extends CFGLine {
    IRMethodCallExpression expression;

    public CFGMethodCall(CFGLine trueBranch, CFGLine falseBranch, IRMethodCallExpression expression) {
        super(trueBranch, falseBranch);
        this.expression = expression;
    }

    public CFGMethodCall(IRMethodCallExpression expression) {
        super();
        this.expression = expression;
    }

    public IRExpression getExpression() { return expression; }

    @Override
    public boolean isNoOp() { return false; }

    @Override
    public String ownValue() {
        return expression.toString();
    }

    @Override
    public <R> R accept(CFGVisitor<R> visitor){
		return visitor.on(this);
	}
}

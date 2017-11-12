package edu.mit.compilers.cfg;
import edu.mit.compilers.cfg.CFGLine;
import edu.mit.compilers.ir.expression.IRExpression;
import java.util.Set;

public class CFGReturn extends CFGLine {
    IRExpression expression; // null if void return statement

    public CFGReturn(CFGLine trueBranch, CFGLine falseBranch, IRExpression expression) {
        super(trueBranch, falseBranch);
        this.expression = expression;
    }

    public CFGReturn(IRExpression expression) {
        super();
        this.expression = expression;
    }

    public IRExpression getExpression() {
        if (expression == null) {
            throw new RuntimeException("Trying to access expression of void return statement. Check isVoid() before calling getExpression() to fix.");
        }
        return expression;
    }

    public boolean isVoid() { return expression == null; }

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

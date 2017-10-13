package edu.mit.compilers.cfg;
import edu.mit.compilers.cfg.CFGLine;
import edu.mit.compilers.ir.expression.IRExpression;

public class CFGExpression extends CFGLine {
    IRExpression expression;

    public CFGExpression(CFGLine trueBranch, CFGLine falseBranch, IRExpression expression) {
        super(trueBranch, falseBranch);
        this.expression = expression;
    }

    public CFGExpression(IRExpression expression) {
        super();
        this.expression = expression;
    }

    public IRExpression getExpression() { return expression; }

    @Override
    public boolean isNoOp() { return false; }

    @Override
    protected String ownValue() {
        return expression.toString();
    }
}

package edu.mit.compilers.cfg;
import edu.mit.compilers.cfg.CFGLine;
import edu.mit.compilers.ir.expression.IRExpression;
import java.util.Set;

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
    public String ownValue() {
        return expression.toString();
    }

    @Override
    public <R> R accept(CFGBitSetVisitor<R> visitor, Set<String> parentSet){
		return visitor.on(this, parentSet);
	}
}

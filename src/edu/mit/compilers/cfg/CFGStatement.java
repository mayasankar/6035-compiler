package edu.mit.compilers.cfg;
import edu.mit.compilers.cfg.CFGLine;
import edu.mit.compilers.ir.statement.IRStatement;

public class CFGStatement extends CFGLine {
    IRStatement statement;

    public CFGStatement(CFGLine trueBranch, CFGLine falseBranch, IRStatement statement) {
        super(trueBranch, falseBranch);
        this.statement = statement;
    }

    public CFGStatement(IRStatement statement) {
        super();
        this.statement = statement;
    }

    public IRStatement getStatement() { return statement; }

    @Override
    public boolean isNoOp() { return false; }
}

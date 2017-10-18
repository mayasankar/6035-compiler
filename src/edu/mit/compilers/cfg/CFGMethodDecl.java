package edu.mit.compilers.cfg;
import edu.mit.compilers.cfg.CFGLine;
import edu.mit.compilers.ir.decl.IRMethodDecl;

public class CFGMethodDecl extends CFGLine {
    IRMethodDecl method;

    public CFGMethodDecl(CFGLine trueBranch, CFGLine falseBranch, IRMethodDecl method) {
        super(trueBranch, falseBranch);
        this.method = method;
    }

    public CFGMethodDecl(IRMethodDecl method) {
        super();
        this.method = method;
    }

    public IRMethodDecl getMethodDecl() { return method; }

    @Override
    public boolean isNoOp() { return false; }

    @Override
    public String ownValue() {
        return method.toString();
    }
}

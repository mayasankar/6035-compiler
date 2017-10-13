package edu.mit.compilers.cfg;
import edu.mit.compilers.cfg.CFGLine;
import edu.mit.compilers.ir.decl.IRMemberDecl;

public class CFGDecl extends CFGLine {
    IRMemberDecl decl;

    public CFGDecl(CFGLine trueBranch, CFGLine falseBranch, IRMemberDecl decl) {
        super(trueBranch, falseBranch);
        this.decl = decl;
    }

    public CFGDecl(IRMemberDecl decl) {
        super();
        this.decl = decl;
    }

    public IRMemberDecl getDecl() { return decl; }

    @Override
    public boolean isNoOp() { return false; }
}

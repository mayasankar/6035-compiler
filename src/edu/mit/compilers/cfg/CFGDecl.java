package edu.mit.compilers.cfg;
import edu.mit.compilers.cfg.CFGLine;
import edu.mit.compilers.cfg.decl.IRMemberDecl;

public class CFGDecl extends CFGLine {
    IRMemberDecl decl;

    public CFGStatement(CFGLine trueBranch, CFGLine falseBranch, IRMemberDecl decl) {
        super(trueBranch, falseBranch);
        this.decl = decl;
    }

    public IRStatement getDecl() { return decl; }
}

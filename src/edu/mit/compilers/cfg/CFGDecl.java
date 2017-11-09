package edu.mit.compilers.cfg;
import edu.mit.compilers.cfg.CFGLine;
import edu.mit.compilers.ir.decl.IRMemberDecl;
import java.util.Set;

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

    public int getLength() { return decl.getLength(); }

    @Override
    public boolean isNoOp() { return false; }

    @Override
    public String ownValue() {
        return decl.toString();
    }

    @Override
    public <R> R accept(CFGVisitor<R> visitor){
		return visitor.on(this);
	}
}

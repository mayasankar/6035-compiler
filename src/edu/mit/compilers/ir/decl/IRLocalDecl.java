package edu.mit.compilers.ir.decl;

import antlr.Token;

import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.IRNode.IRNodeVisitor;

public class IRLocalDecl extends IRMemberDecl {

	// NOTE -- is this ever even used?

	public IRLocalDecl(IRType.Type irType, Token id) {
		super(irType, id);
	}

	public IRLocalDecl(IRType.Type irType, Token id, int length) {
		super(irType, id, length);
	}

	@Override
	public String toString() {
		return this.getType() + " " + this.getName();
	}

	@Override
	public <R> R accept(IRNodeVisitor<R> visitor) {
		return visitor.on(this);
	}
	
}

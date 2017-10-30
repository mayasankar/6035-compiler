package edu.mit.compilers.ir.decl;

import antlr.Token;

import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.IRNode.IRNodeVisitor;

public class IRFieldDecl extends IRMemberDecl {
	// NOTE (mayars) -- do we even need this class? It is the only subclass of IRMemberDecl

	public IRFieldDecl(IRType.Type irType, Token id) {
		super(irType, id);
	}

	public IRFieldDecl(IRType.Type irType, Token id, int length) {
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

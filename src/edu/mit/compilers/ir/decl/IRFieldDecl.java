package edu.mit.compilers.ir.decl;

import antlr.Token;

import edu.mit.compilers.ir.IRType;

public class IRFieldDecl extends IRMemberDecl {

	public IRFieldDecl(IRType irType, Token id) {
		super(irType, id);
	}

	@Override
	public String toString() {
		return this.getType() + " " + this.getName();
	}

}

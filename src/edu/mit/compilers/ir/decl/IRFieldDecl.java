package edu.mit.compilers.ir.decl;

import antlr.Token;

import edu.mit.compilers.ir.IRType;

public class IRFieldDecl extends IRMemberDecl {

	protected IRType irType;

	public IRFieldDecl(IRType irType, Token id) {
		super(id);
		this.irType = irType;
	}

	public IRType getType() {
		return irType;
	}

	@Override
	public String toString() {
		return irType + " " + getName();
	}

}

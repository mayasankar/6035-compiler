package edu.mit.compilers.ir.decl;

import antlr.Token;

import edu.mit.compilers.ir.IRType;

public class IRParameterDecl extends IRMemberDecl {

	public IRParameterDecl(IRType.Type irType, Token id) {
		super(irType, id);
	}

	public IRParameterDecl(IRType.Type irType, Token id, int length) {
		super(irType, id, length);
	}

	@Override
	public String toString() {
		return this.getType() + " " + this.getName();
	}
}

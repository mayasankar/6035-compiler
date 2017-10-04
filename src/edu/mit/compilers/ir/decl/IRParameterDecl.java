package edu.mit.compilers.ir.decl;

import antlr.Token;

import edu.mit.compilers.ir.IRType;

public class IRParameterDecl extends IRMemberDecl {

	protected IRType.Type type;

	public IRParameterDecl(IRType.Type type, Token id) {
		super(id);
		this.type = type;
	}

	@Override
	public String toString() {
		return type.name() + " " + getName();
	}
}

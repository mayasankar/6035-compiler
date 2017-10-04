package edu.mit.compilers.ir.decl;

import antlr.Token;

import edu.mit.compilers.ir.IRType;

public class IRFieldDecl extends IRMemberDecl {

	public IRFieldDecl(IRType type, Token id) {
		super(type, id);
	}

}

package edu.mit.compilers.ir.decl;

import antlr.Token;

import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class IRParameterDecl extends IRMemberDecl {

	public IRParameterDecl(TypeDescriptor irType, Token id) {
		super(irType, id);
	}

	public IRParameterDecl(TypeDescriptor irType, Token id, int length) {
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

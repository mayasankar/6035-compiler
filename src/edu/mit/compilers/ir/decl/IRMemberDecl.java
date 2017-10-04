package edu.mit.compilers.ir.decl;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;

public abstract class IRMemberDecl extends IRNode {

	protected IRType type;
	protected Token id;

	// @Override
	// public List<? extends IRNode> getChildren() {
	// 	return Arrays.asList();
	// }

	protected IRMemberDecl(IRType type, Token id) {
		this.type = type;
		this.id = id;
	}

	public IRType getType() {
		return type;
	}

	public String getName() {
		return id.getText();
	}

	@Override
	public String toString() {
		return type + " " + getName();
	}

}

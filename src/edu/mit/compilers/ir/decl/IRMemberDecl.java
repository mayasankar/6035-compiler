package edu.mit.compilers.ir.decl;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;

public abstract class IRMemberDecl extends IRNode {

	protected Token id;

	// @Override
	// public List<? extends IRNode> getChildren() {
	// 	return Arrays.asList();
	// }

	protected IRMemberDecl(Token id) {
		this.id = id;
	}

	public String getName() {
		return id.getText();
	}

}

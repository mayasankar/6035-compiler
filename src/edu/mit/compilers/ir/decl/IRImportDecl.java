package edu.mit.compilers.ir.decl;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;

public class IRImportDecl extends IRNode {
	protected Token id;

	public IRImportDecl(Token id) {
		setLineNumbers(id);
		this.id = id;
	}

	public String getName() {
		return id.getText();
	}

	@Override
	public String toString(){
		return getName();
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList();
	}
}

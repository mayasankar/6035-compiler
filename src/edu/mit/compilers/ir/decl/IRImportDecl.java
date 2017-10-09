package edu.mit.compilers.ir.decl;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;

public class IRImportDecl extends IRMethodDecl {

	public IRImportDecl(Token id) {
		super(id);
	}

	@Override
	public boolean isImport() { return true; }

	@Override
	public String toString(){
		return id.getText();
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList();
	}
}

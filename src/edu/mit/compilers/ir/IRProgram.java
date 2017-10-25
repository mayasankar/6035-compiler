package edu.mit.compilers.ir;

import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.symbol_tables.*;

import antlr.Token;

import java.util.List;
import java.util.ArrayList;

public class IRProgram extends IRNode {
	public List<IRImportDecl> imports; // Token.getText() gets the name of the token

	public IRProgram(List<IRImportDecl> imports, VariableTable fields, MethodTable methods) {
		this.imports = imports;
		this.variableScope = fields;
		this.methodTable = methods;
	}

	@Override
	public String toString() {
		String answer = "Imports: ";
		for (IRImportDecl imp : imports) {
			answer += imp.getName() + ", ";
		}
		answer += "\n" + variableScope.toString();
		answer += "\n" + methodTable.toString();
		return answer;
	}

	@Override
	public List<? extends IRNode> getChildren() {
		ArrayList<IRNode> children = new ArrayList<IRNode>(imports);
		children.addAll(variableScope.getVariableList());
		children.addAll(methodTable.getMethodList());
		return children;
	}
}

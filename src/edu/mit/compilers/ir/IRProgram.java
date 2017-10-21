package edu.mit.compilers.ir;

import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.symbol_tables.*;

import antlr.Token;

import java.util.List;
import java.util.ArrayList;

public class IRProgram extends IRNode {
	public List<IRImportDecl> imports; // Token.getText() gets the name of the token
	public VariableTable fields;
	public MethodTable methods;

	public IRProgram(List<IRImportDecl> imports, VariableTable fields, MethodTable methods) {
		this.imports = imports;
		this.fields = fields;
		this.methods = methods;
	}

	public IRProgram(ConcreteTree tree) {
		setLineNumbers(tree);

		imports = new ArrayList<>();
		fields = new VariableTable();
		methods = new MethodTable();

		ConcreteTree child = tree.getFirstChild(); // TODO do I need to instantiate these?
		while (child != null && child.getName().equals("import_decl")) {
			IRImportDecl imp = new IRImportDecl(child.getFirstChild().getToken());
			imports.add(imp); // todo maybe remove
			methods.add(imp);
			child = child.getRightSibling();
		}

		while (child != null && child.getName().equals("field_decl")) {
			ConcreteTree grandchild = child.getFirstChild();
			Token typeToken = grandchild.getToken();
			grandchild = grandchild.getRightSibling();
			while (grandchild != null) {
				Token id = grandchild.getFirstChild().getToken();
				if (grandchild.getFirstChild() != grandchild.getLastChild()) {
					Token length = grandchild.getFirstChild().getRightSibling().getRightSibling().getToken();
					int lengthAsInt = Integer.parseInt(length.getText());
					fields.add(new VariableDescriptor(new IRFieldDecl(IRType.getType(typeToken, lengthAsInt), id, lengthAsInt)));
				} else {
					fields.add(new VariableDescriptor(new IRFieldDecl(IRType.getType(typeToken), id)));
				}
				grandchild = grandchild.getRightSibling();
			}
			child = child.getRightSibling();
		}

		while (child != null && child.getName().equals("method_decl")) {
			methods.add(new IRMethodDecl(child, fields));
			child = child.getRightSibling();
		}
	}

	@Override
	public String toString() {
		String answer = "Imports: ";
		for (IRImportDecl imp : imports) {
			answer += imp.getName() + ", ";
		}
		answer += "\n" + fields.toString();
		answer += "\n" + methods.toString();
		return answer;
	}

	@Override
	public List<? extends IRNode> getChildren() {
		ArrayList<IRNode> children = new ArrayList<IRNode>(imports);
		children.addAll(fields.getVariableList());
		children.addAll(methods.getMethodList());
		return children;
	}
}

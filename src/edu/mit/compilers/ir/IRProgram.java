package edu.mit.compilers.ir;

import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.symbol_tables.*;

import antlr.Token;

import java.util.ArrayList;

public class IRProgram extends IRNode {
	public ArrayList<IRImportDecl> imports; // Token.getText() gets the name of the token
	public VariableTable fields;
	public MethodTable methods;

	public IRProgram(ConcreteTree tree) {
		imports = new ArrayList<>();
		fields = new VariableTable();
		methods = new MethodTable();

		System.out.println("Starting IRProgram");

		ConcreteTree child = tree.getFirstChild(); // TODO do I need to instantiate these?
		while (child != null && child.getName().equals("import_decl")) {
			imports.add(new IRImportDecl(child.getFirstChild().getToken()));
			child = child.getRightSibling();
		}
		System.out.println("Did imports");

		while (child != null && child.getName().equals("field_decl")) {
			ConcreteTree grandchild = child.getFirstChild();
			Token typeToken = grandchild.getToken();
			grandchild = grandchild.getRightSibling();
			while (grandchild != null) {
				Token id = grandchild.getFirstChild().getToken();
				if (grandchild.getFirstChild() != grandchild.getLastChild()) {
					Token length = grandchild.getFirstChild().getRightSibling().getRightSibling().getToken();
					int lengthAsInt = Integer.parseInt(length.getText());
					fields.add(new IRFieldDecl(IRType.getType(typeToken), id)); // TODO Jackie broke arrays and will fix them
				} else {
					fields.add(new IRFieldDecl(IRType.getType(typeToken), id));
				}
				grandchild = grandchild.getRightSibling();
			}
			child = child.getRightSibling();
		}

		System.out.println("Did fields");

		while (child != null && child.getName().equals("method_decl")) {
			methods.add(new IRMethodDecl(child, fields));
			System.out.println("Finished a declaration");
			child = child.getRightSibling();
		}
		System.out.println("Did methods");
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
}

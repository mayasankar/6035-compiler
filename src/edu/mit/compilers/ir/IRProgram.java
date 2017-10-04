package edu.mit.compilers.ir;

import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.trees.ConcreteTree;

import antlr.Token;

import java.util.ArrayList;

public class IRProgram extends IRNode {
	public ArrayList<Token> imports; // Token.getText() gets the name of the token
	public ArrayList<IRFieldDecl> fields;
	public ArrayList<IRMethodDecl> methods;

	public IRProgram(ConcreteTree tree) {
		ConcreteTree child = tree.getFirstChild(); // TODO do I need to instantiate these?
		imports = new ArrayList<Token>();
		while (child != null && child.getName().equals("import_decl")) {
			imports.add(child.getFirstChild().getToken());
			child = child.getRightSibling();
		}
		fields = new ArrayList<IRFieldDecl>();
		while (child != null && child.getName().equals("field_decl")) {
			ConcreteTree grandchild = child.getFirstChild();
			Token typeToken = grandchild.getToken();
			grandchild = grandchild.getRightSibling();
			while (grandchild != null) {
				Token id = grandchild.getFirstChild().getToken();
				if (grandchild.getFirstChild() != grandchild.getLastChild()) {
					Token length = grandchild.getFirstChild().getRightSibling().getRightSibling().getToken();
					int lengthAsInt = Integer.parseInt(length.getText());
					fields.add(new IRFieldDecl(new IRType(typeToken, lengthAsInt), id));
				} else {
					fields.add(new IRFieldDecl(new IRType(typeToken), id));
				}
				grandchild = grandchild.getRightSibling();
			}
			child = child.getRightSibling();
		}
		methods = new ArrayList<IRMethodDecl>();
		while (child != null && child.getName().equals("method_decl")) {
			methods.add(new IRMethodDecl(child));
			child = child.getRightSibling();
		}
	}

	@Override
	public String toString() {
		String answer = "Imports: ";
		for (Token imp : imports) {
			answer += imp.getText() + ", ";
		}
		answer += "\nFields: ";
		for (IRFieldDecl field : fields) {
			answer += field.toString() + ", ";
		}
		answer += "\nMethods: ";
		for (IRMethodDecl method : methods) {
			answer += "" + " "; // TODO fix
		}
		return answer;
	}
}

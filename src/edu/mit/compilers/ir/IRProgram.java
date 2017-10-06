package edu.mit.compilers.ir;

import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.symbol_tables.ProgramTable;

import antlr.Token;

import java.util.ArrayList;

public class IRProgram extends IRNode {
	//public ArrayList<Token> imports; // Token.getText() gets the name of the token
	//public ArrayList<IRFieldDecl> fields;
	//public ArrayList<IRMethodDecl> methods;
	public ProgramTable table;

	public IRProgram(ConcreteTree tree) {

		table = new ProgramTable();

		ConcreteTree child = tree.getFirstChild(); // TODO do I need to instantiate these?
		//imports = new ArrayList<Token>();
		while (child != null && child.getName().equals("import_decl")) {
			table.addImport(child.getFirstChild().getToken());
			//imports.add(child.getFirstChild().getToken());
			child = child.getRightSibling();
		}

		//fields = new ArrayList<IRFieldDecl>();
		while (child != null && child.getName().equals("field_decl")) {
			ConcreteTree grandchild = child.getFirstChild();
			Token typeToken = grandchild.getToken();
			grandchild = grandchild.getRightSibling();
			while (grandchild != null) {
				Token id = grandchild.getFirstChild().getToken();
				if (grandchild.getFirstChild() != grandchild.getLastChild()) {
					Token length = grandchild.getFirstChild().getRightSibling().getRightSibling().getToken();
					int lengthAsInt = Integer.parseInt(length.getText());
					table.addField(new IRFieldDecl(new IRType(typeToken, lengthAsInt), id));
					//fields.add(new IRFieldDecl(new IRType(typeToken, lengthAsInt), id));
				} else {
					table.addField(new IRFieldDecl(new IRType(typeToken), id));
					//fields.add(new IRFieldDecl(new IRType(typeToken), id));
				}
				grandchild = grandchild.getRightSibling();
			}
			child = child.getRightSibling();
		}

		//methods = new ArrayList<IRMethodDecl>();
		while (child != null && child.getName().equals("method_decl")) {
			table.addMethod(new IRMethodDecl(child, table.getFields()));
			//methods.add(new IRMethodDecl(child));
			child = child.getRightSibling();
		}
	}

	public ProgramTable getProgramTable(){
		return this.table;
	}

	@Override
	public String toString() {
		return this.table.toString();
	}

	// @Override
	// public String toString() {
	// 	String answer = "Imports: ";
	// 	for (Token imp : imports) {
	// 		answer += imp.getText() + ", ";
	// 	}
	// 	answer += "\nFields: ";
	// 	for (IRFieldDecl field : fields) {
	// 		answer += field.toString() + ", ";
	// 	}
	// 	answer += "\nMethods: \n";
	// 	for (IRMethodDecl method : methods) {
	// 		answer += method.toString() + "\n";
	// 	}
	// 	return answer;
	// }
}

package edu.mit.compilers.ir.statement;

import java.util.ArrayList;
import java.util.Collections;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.decl.IRFieldDecl;
import edu.mit.compilers.ir.statement.IRStatement;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.symbol_tables.VariableTable;

public class IRBlock extends IRNode {

	//private ArrayList<IRFieldDecl> fields = new ArrayList<IRFieldDecl>();
	private ArrayList<IRStatement> statements = new ArrayList<IRStatement>();
	private VariableTable fields;

	public IRBlock(ConcreteTree tree, VariableTable parentScope) {
		fields = new VariableTable(parentScope);
		ConcreteTree child = tree.getFirstChild();
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
		while (child != null) {
			statements.add(IRStatement.makeIRStatement(child, parentScope));
			child = child.getRightSibling();
		}
	}

	public ArrayList<IRStatement> getStatements(){
		return this.statements;
	}

	public VariableTable getFields(){
		return this.fields;
	}

	@Override
	public String toString() {
		return toString(0);
	}

	public String toString(int indent) {
		String answer = "\n";
		for (int i = 0; i < indent; ++i) {
			answer += "  ";
		}
		answer += fields.toString();
		for (IRStatement statement : statements) {
			answer += "\n" + statement.toString(indent);
		}
		return answer;
	}

}

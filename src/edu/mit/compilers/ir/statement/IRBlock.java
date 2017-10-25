package edu.mit.compilers.ir.statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.decl.IRFieldDecl;
import edu.mit.compilers.ir.statement.IRStatement;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.symbol_tables.VariableTable;
import edu.mit.compilers.symbol_tables.VariableDescriptor;

public class IRBlock extends IRNode {

	private List<IRFieldDecl> fieldDecls = new ArrayList<IRFieldDecl>();
	private List<IRStatement> statements = new ArrayList<IRStatement>();
	private VariableTable fields;

	public IRBlock(List<IRFieldDecl> fieldDecls, List<IRStatement> statements, VariableTable fields) {
		this.fieldDecls = fieldDecls;
		this.statements = statements;
		this.fields = fields;
	}

	public List<IRStatement> getStatements(){
		return this.statements;
	}

	public VariableTable getFields(){
		return this.fields;
	}

	public List<IRFieldDecl> getFieldDecls() { return this.fieldDecls; }

	@Override
	public List<? extends IRNode> getChildren() {
		ArrayList<IRNode> children = new ArrayList<IRNode>(statements);
		children.addAll(fields.getVariableList());
		return children;
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

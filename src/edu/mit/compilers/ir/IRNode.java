package edu.mit.compilers.ir;

import java.util.List;

import antlr.Token;
import edu.mit.compilers.symbol_tables.MethodTable;
import edu.mit.compilers.symbol_tables.VariableTable;
import edu.mit.compilers.trees.ConcreteTree;

public abstract class IRNode {
	public int line = -1;
	public int column = -1;
	protected VariableTable variableScope;
	protected MethodTable methodTable;

	public String location(){
		return Integer.toString(line) + "," + Integer.toString(column);
	}

	public void setLineNumbers(int line, int column) {
		this.line = line;
		this.column = column;
	}

	public void setLineNumbers(ConcreteTree tree) {
		line = tree.getLine();
		column = tree.getColumn();
	}

	public void setLineNumbers(Token tk) {
		line = tk.getLine();
		column = tk.getColumn();
	}

	public void setLineNumbers(IRNode node) {
		this.line = node.line;
		this.column = node.column;
	}

	// returns true if this comes after other, via line + col numbers.
	// returns false if other = this.
	public boolean comesAfter(IRNode other) {
		if (this.line < 0 || other.line < 0) {
			throw new RuntimeException("Comparing IRNode with uninitialized line number");
		} else if (this.line != other.line) {
			return this.line > other.line;
		} else {
			if (this.column < 0 || other.column < 0) {
				throw new RuntimeException("Comparing IRNode with uninitialized column number");
			}
			return this.column > other.column;
		}
	}

	public abstract List<? extends IRNode> getChildren();

	public void setTables(VariableTable varTable, MethodTable methodTable) {
		this.variableScope = varTable;
		this.methodTable = methodTable;
	}

	public MethodTable getMethodTable() {
		return methodTable;
	}

	public VariableTable getVariableTable() {
		return variableScope;
	}
}

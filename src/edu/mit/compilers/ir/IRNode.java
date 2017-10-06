package edu.mit.compilers.ir;

import java.util.List;

import antlr.Token;

import edu.mit.compilers.trees.ConcreteTree;

public abstract class IRNode {
	public int line = -1;
	public int column = -1;

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

	public abstract List<? extends IRNode> getChildren();
}

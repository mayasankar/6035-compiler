package edu.mit.compilers.ir.statement;

import java.util.ArrayList;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.trees.ConcreteTree;

public class IRBlock {

	private ArrayList<IRStatement> statements;

	public IRBlock(ConcreteTree tree) {
		if (!tree.getName().equals("block")) {
			System.out.println("Error: tree name is " + tree.getName());
		}
	}

	// @Override
	// public List<? extends IRNode> getChildren() {
	// 	return Collections.unmodifiableList(statements);
	// }

}

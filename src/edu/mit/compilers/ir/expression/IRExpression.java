package edu.mit.compilers.ir.expression;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.trees.ConcreteTree;

public abstract class IRExpression extends IRNode {
	public abstract IRType getType();

	public static IRExpression makeIRExpression(ConcreteTree tree) {
		//TODO fix
		return null;
	}
}

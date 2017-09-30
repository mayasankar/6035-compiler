package edu.mit.compilers.ir.expression;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;

public abstract class IRExpression extends IRNode {
	public abstract IRType getType();
}

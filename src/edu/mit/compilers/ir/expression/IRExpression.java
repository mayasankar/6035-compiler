package edu.mit.compilers.ir.expression;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.trees.ConcreteTree;

public abstract class IRExpression extends IRNode {
	public abstract IRType getType();

	public static IRExpression makeIRExpression(ConcreteTree tree) {
		String exprType = tree.getName();
		// expr_base, expr_0 to expr_8.
		if (exprType.equals("expr_base")) {
			// TODO this case
			return null;
		} else if (exprType.equals("expr_1")) {
			return new IRUnaryOpExpression();
		} else if (exprType.equals("expr_2") ||
						   exprType.equals("expr_3") ||
						   exprType.equals("expr_4") ||
							 exprType.equals("expr_5") ||
							 exprType.equals("expr_6") ||
							 exprType.equals("expr_7")) {
			return new IRBinaryOpExpression();
		} else if (exprType.equals("expr_8")) {
			return new IRTernaryOpExpression();
		}
		//TODO throw an error if control reaches here.
		return null;
	}
}

package edu.mit.compilers.ir.expression;

import antlr.Token;
import java.math.BigInteger;
import java.util.List;

import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.trees.ASTCreator;
import edu.mit.compilers.ir.expression.literal.*;

public abstract class IRExpression extends IRNode {
	public abstract IRType.Type getType();

	public static enum ExpressionType {
		UNSPECIFIED,
		BOOL_LITERAL,
		INT_LITERAL,
		STRING_LITERAL,
		UNARY,
		BINARY,
		TERNARY,
		LEN,
		METHOD_CALL,
		VARIABLE,
	}

	protected ExpressionType expressionType = ExpressionType.UNSPECIFIED;

	public ExpressionType getExpressionType() { return expressionType; }
	
	public abstract int getDepth();
	
	@Override
	public abstract List<IRExpression> getChildren();
}

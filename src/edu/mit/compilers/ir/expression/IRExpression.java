package edu.mit.compilers.ir.expression;

import antlr.Token;
import java.math.BigInteger;

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

	public static IRExpression makeIRExpression(ConcreteTree tree) {
		return ASTCreator.parseExpressionTree(tree);
	}

	public static IRExpression makeIRExpressionOld(ConcreteTree tree) {
		if (tree.isNode()) { // TODO deal with case that string has escaped characters
			String text = tree.getToken().getText();
			IRExpression toReturn = new IRStringLiteral(text.substring(1, text.length()-1));
			toReturn.setLineNumbers(tree);
			return toReturn;
		}
		String exprType = tree.getName();
		if (exprType.equals("expr_base")) {
			ConcreteTree child = tree.getFirstChild();
			if (child.isNode()) {
				IRExpression answer = new IRLenExpression(child.getRightSibling().getToken());
				answer.setLineNumbers(child);
				return answer;
			} else {
				exprType = child.getName();
				if (exprType.equals("method_call")) {
					return new IRMethodCallExpression(child);
				} else if (exprType.equals("literal")) {
					ConcreteTree grandchild = child.getFirstChild();
					Token token = grandchild.getToken();
					int tokentype = token.getType();
					IRExpression toReturn = null;
					if (tokentype == DecafParserTokenTypes.INT) {
						String numAsString = token.getText();
						int radix = 10;
						if (numAsString.length() > 1 && numAsString.substring(0,2).equals("0x")) {
							numAsString = numAsString.substring(2);
							radix = 16;
						}
						toReturn = new IRIntLiteral(new BigInteger(numAsString, radix));
					} else if (tokentype == DecafParserTokenTypes.CHAR) {
						String charstring = token.getText();
						charstring = charstring.substring(1, charstring.length()-1);
						char character = charstring.charAt(0);
						if (character == '\\') {
							if (charstring.equals("\\n")) {
								character = '\n';
							} else if (charstring.equals("\\t")) {
								character = '\t';
							} else {
								character = charstring.charAt(1);
							}
						}
						toReturn = new IRIntLiteral(BigInteger.valueOf((int) character));
					} else if (tokentype == DecafParserTokenTypes.TK_true) {
						toReturn =  new IRBoolLiteral(true);
					} else if (tokentype == DecafParserTokenTypes.TK_false) {
						toReturn = new IRBoolLiteral(false);
					}
					if (toReturn != null) {
						toReturn.setLineNumbers(token);
						return toReturn;
					}
				} else if (exprType.equals("location")) {
					return IRVariableExpression.makeIRVariableExpression(child);
				}
			}
		} else if (exprType.equals("expr_1")) {
			return new IRUnaryOpExpression(tree);
		} else if (exprType.equals("expr_2") ||
						   exprType.equals("expr_3") ||
						   exprType.equals("expr_4") ||
							 exprType.equals("expr_5") ||
							 exprType.equals("expr_6") ||
							 exprType.equals("expr_7")) {
			return new IRBinaryOpExpression(tree);
		} else if (exprType.equals("expr_8")) {
			return new IRTernaryOpExpression(tree);
		}
		throw new RuntimeException("Cannot parse ConcreteTree of type " + tree.getName());
	}
}

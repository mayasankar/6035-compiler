package edu.mit.compilers.ir.expression;

import antlr.Token;

import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.ir.expression.literal.*;

public abstract class IRExpression extends IRNode {
	public abstract IRType.Type getType();

	public static IRExpression makeIRExpression(ConcreteTree tree) {
		String exprType = tree.getName();
		if (exprType.equals("expr_base")) {
			ConcreteTree child = tree.getFirstChild();
			if (child.isNode()) {
				return new IRLenExpression(child.getRightSibling().getToken());
			} else {
				exprType = child.getName();
				if (exprType.equals("method_call")) {
					return new IRMethodCallExpression(child);
				} else if (exprType.equals("literal")) {
					ConcreteTree grandchild = child.getFirstChild();
					Token token = grandchild.getToken();
					int tokentype = token.getType();
					if (tokentype == DecafParserTokenTypes.INT) {
						// TODO make sure this never throws an error parsing
						return new IRIntLiteral(Integer.parseInt(token.getText()));
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
						return new IRIntLiteral((int) character);
					} else if (tokentype == DecafParserTokenTypes.TK_true) {
						return new IRBoolLiteral(true);
					} else if (tokentype == DecafParserTokenTypes.TK_false) {
						return new IRBoolLiteral(false);
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
		//TODO can also be called on string tokens
		//TODO throw an error if control reaches here.
		return null;
	}
}

package edu.mit.compilers.trees;

import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

import antlr.Token;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.expression.IRBinaryOpExpression;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRLenExpression;
import edu.mit.compilers.ir.expression.IRMethodCallExpression;
import edu.mit.compilers.ir.expression.IRTernaryOpExpression;
import edu.mit.compilers.ir.expression.IRUnaryOpExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;
import edu.mit.compilers.ir.expression.literal.IRBoolLiteral;
import edu.mit.compilers.ir.expression.literal.IRIntLiteral;
import edu.mit.compilers.ir.expression.literal.IRStringLiteral;
import edu.mit.compilers.ir.statement.IRAssignStatement;
import edu.mit.compilers.ir.statement.IRLoopStatement;
import edu.mit.compilers.ir.statement.IRMethodCallStatement;
import edu.mit.compilers.ir.statement.IRReturnStatement;
import edu.mit.compilers.ir.statement.IRStatement;
import edu.mit.compilers.ir.statement.IRStatement.StatementType;

// This class has a lot of the functions necessary to simplify the concrete tree
// into an abstract tree.

public class ASTCreator {

  public static void simplifyTree(ConcreteTree tree) {
    tree.initializeLineNumbers();
    // delete tokens only necessary for parsing
    tree.deleteNodes(DecafParserTokenTypes.EOF);
    tree.deleteNodes(DecafParserTokenTypes.COMMA);
    tree.deleteNodes(DecafParserTokenTypes.LPAREN);
    tree.deleteNodes(DecafParserTokenTypes.RPAREN);
    tree.deleteNodes(DecafParserTokenTypes.LCURLY);
    tree.deleteNodes(DecafParserTokenTypes.RCURLY);
    tree.deleteNodes(DecafParserTokenTypes.SEMICOLON);
    tree.deleteNodes(DecafParserTokenTypes.TK_import);
    tree.deleteNodes(DecafParserTokenTypes.TK_if);
    tree.deleteNodes(DecafParserTokenTypes.TK_else);
    tree.deleteNodes(DecafParserTokenTypes.TK_for);
    tree.deleteNodes(DecafParserTokenTypes.TK_while);
    tree.deleteNodes(DecafParserTokenTypes.OP_TERN_1);
    tree.deleteNodes(DecafParserTokenTypes.OP_TERN_2);
    // contract along unnecessary edges
    tree.compressNodes("type");
    tree.compressNodes("op_pm");
    tree.compressNodes("bool_literal");
    tree.compressNodes("expr");
    for (int i = 0; i <= 8; ++i) {
      tree.compressNodes("expr_" + i);
    }
  }

  public static IRProgram getIR(ConcreteTree tree) {
    simplifyTree(tree);
    return new IRProgram(tree);
  }

  public static IRExpression parseExpressionTree(ConcreteTree tree) {
  	String nodeName = tree.getName();
  	switch(nodeName) {
		case "expr_0":
			return parseExpressionTree(tree.getFirstChild());
		case "expr_1":
			IRExpression returnValue = parseExpressionTree(tree.getLastChild());
			ConcreteTree subTree = tree.getLastChild().getLeftSibling();
			while(subTree != null) {
				returnValue = new IRUnaryOpExpression(subTree.getToken(), returnValue);
			}
			return returnValue;
		case "expr_2":
		case "expr_3":
		case "expr_4":
		case "expr_5":
		case "expr_6":
		case "expr_7": {
			ConcreteTree nextNode = tree.getFirstChild();
			IRExpression returnExpression = parseExpressionTree(nextNode);
			while(nextNode.getRightSibling() != null) {
				ConcreteTree operator = tree.getRightSibling();
				nextNode = tree.getRightSibling();
				if (nextNode == null) {
					throw new RuntimeException("This binary operator node does not have odd number of children.");
				}
				IRExpression nextTerm = parseExpressionTree(nextNode);
				returnExpression = new IRBinaryOpExpression(returnExpression, operator.getToken(), nextTerm);
			}
			return returnExpression;
		}
		case "expr_8":
			ConcreteTree nextNode = tree.getLastChild();
			IRExpression returnExpression = parseExpressionTree(nextNode);
			while(nextNode.getRightSibling() != null) {
				ConcreteTree trueExpressionTree = nextNode.getLeftSibling().getLeftSibling();
				ConcreteTree conditionTree = trueExpressionTree.getLeftSibling().getLeftSibling();
				IRExpression trueExpression = parseExpressionTree(trueExpressionTree);
				IRExpression condition = parseExpressionTree(conditionTree);
				returnExpression = new IRTernaryOpExpression(condition, trueExpression, returnExpression);
			}
			return returnExpression;
		case "expr_base":
			ConcreteTree firstChild = tree.getFirstChild();
			if(firstChild.isNode()) {
				return new IRLenExpression(firstChild.getRightSibling().getToken());
			}
			else if(firstChild.getName() == "method_call") {
				List<IRExpression> arguments = new ArrayList<>();
				ConcreteTree nextChild = firstChild.getRightSibling();
				while (nextChild != null) {
					arguments.add(parseExpressionTree(nextChild));
					nextChild = nextChild.getRightSibling();
				}
				return new IRMethodCallExpression(firstChild.getToken().getText(), arguments);
			}
			else if(firstChild.getName() == "location") {
				return parseLocation(firstChild);
			}
			else if(firstChild.getName() == "literal") {
				ConcreteTree literalValue = firstChild.getFirstChild();
				Token token = literalValue.getToken();
				int tokentype = token.getType();
				IRExpression toReturn = null;
				if (tokentype == DecafParserTokenTypes.INT) {
					toReturn = new IRIntLiteral(new BigInteger(token.getText()));
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
				if (toReturn == null) {
					throw new RuntimeException("Literal " + token.getText() + " could not be parsed.");
				}
				toReturn.setLineNumbers(token);
				return toReturn;
			}
		default:
			return new IRStringLiteral(tree.getToken().getText());
		}
  }

  public static IRVariableExpression parseLocation(ConcreteTree tree) {
		if (tree == null) {
			throw new RuntimeException("Location node is empty");
		}
		ConcreteTree child = tree.getFirstChild();
		String name = child.getToken().getText();
		child = child.getRightSibling();
		if (child == null) {
			return new IRVariableExpression(name);
		} else {
			child = child.getRightSibling();
			return new IRVariableExpression(name, parseExpressionTree(child));
		}
  }

  public static IRStatement parseStatement(ConcreteTree tree) {
    ConcreteTree child = tree.getFirstChild();
    IRStatement toReturn = null;
  	if (child.isNode()) {
      int tokentype = child.getToken().getType();
      if (tokentype == DecafParserTokenTypes.TK_return) {
      	IRExpression returnExpression = parseExpressionTree(child.getRightSibling());
        toReturn = new IRReturnStatement(returnExpression);
      } else if (tokentype == DecafParserTokenTypes.TK_break) {
        toReturn = IRLoopStatement.breakStatement;
      } else if (tokentype == DecafParserTokenTypes.TK_continue) {
        toReturn = IRLoopStatement.continueStatement;
      }
    }

  	String nodeName = tree.getName();
  	switch(nodeName) {
  	case "assign_expr":
  		IRVariableExpression location = parseLocation(child);
  		child = child.getRightSibling();
  		Token operator = child.getToken();
  		child = child.getRightSibling();
  		IRExpression assignment = null;
  		if(child != null) {
  			assignment = parseExpressionTree(child);
  		}
  		toReturn = new IRAssignStatement(location, operator, assignment);
  		break;
  	case "method_call":
  		IRExpression methodCall = parseExpressionTree(tree);
  		toReturn = new IRMethodCallStatement(methodCall);
  	case "if_block"://TODO: finish for Arkadiy

  	case "for_block":
  	case "while_block":
  	}

  	return toReturn;
  }



}

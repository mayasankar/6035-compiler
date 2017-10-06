package edu.mit.compilers.trees;

import java.util.ArrayList;
import java.util.List;

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
    tree.compressNodes("expr");
    for (int i = 0; i <= 8; ++i) {
      tree.compressNodes("expr_" + i);
    }
    tree.compressNodes("op_pm");
    tree.compressNodes("bool_literal");
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
  	default:
  		throw new RuntimeException("A node with name " + nodeName + " cannot be parsed as an expression!");
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

}

package edu.mit.compilers.trees;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

import antlr.Token;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.IRFieldDecl;
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
import edu.mit.compilers.ir.statement.IRBlock;
import edu.mit.compilers.ir.statement.IRForStatement;
import edu.mit.compilers.ir.statement.IRIfStatement;
import edu.mit.compilers.ir.statement.IRLoopStatement;
import edu.mit.compilers.ir.statement.IRMethodCallStatement;
import edu.mit.compilers.ir.statement.IRReturnStatement;
import edu.mit.compilers.ir.statement.IRStatement;
import edu.mit.compilers.ir.statement.IRStatement.StatementType;
import edu.mit.compilers.ir.statement.IRWhileStatement;
import edu.mit.compilers.symbol_tables.VariableTable;

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
  
  public static IRMethodCallExpression parseMethodExpression(ConcreteTree tree) {
      List<IRExpression> arguments = new ArrayList<>();
      String methodName = tree.getFirstChild().getToken().getText();
      ConcreteTree nextChild = tree.getFirstChild().getRightSibling();
      while (nextChild != null) {
          arguments.add(parseExpressionTree(nextChild));
          nextChild = nextChild.getRightSibling();
      }
      return new IRMethodCallExpression(methodName, arguments);
  }
  
  // TODO for mayars -- add line numbers to this function.
  public static IRExpression parseExpressionTree(ConcreteTree tree) {
  	String nodeName = tree.getName();
  	//System.out.println("Parsing expression with node name: " + nodeName);
  	switch(nodeName) {
	case "expr_0":
		return parseExpressionTree(tree.getFirstChild());
	case "expr_1":
		IRExpression returnValue = parseExpressionTree(tree.getLastChild());
		ConcreteTree subTree = tree.getLastChild().getLeftSibling();
		if (returnValue.getExpressionType() == IRExpression.ExpressionType.INT_LITERAL) {
	        IRIntLiteral literalValue = (IRIntLiteral) returnValue;
	        while (subTree != null && subTree.getToken().getType() == DecafParserTokenTypes.OP_NEG) {
	            literalValue.invert();
	            subTree = subTree.getLeftSibling();
	        }
	    }
		while(subTree != null) {
			returnValue = new IRUnaryOpExpression(subTree.getToken(), returnValue);
			subTree = subTree.getLeftSibling();
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
			ConcreteTree operator = nextNode.getRightSibling();
			nextNode = operator.getRightSibling();
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
		while(nextNode.getLeftSibling() != null) {
			ConcreteTree trueExpressionTree = nextNode.getLeftSibling();
			ConcreteTree conditionTree = trueExpressionTree.getLeftSibling();
			nextNode = conditionTree;
			IRExpression trueExpression = parseExpressionTree(trueExpressionTree);
			IRExpression condition = parseExpressionTree(conditionTree);
			returnExpression = new IRTernaryOpExpression(condition, trueExpression, returnExpression);
		}
		return returnExpression;
	case "expr_base":
        if (tree.isNode()) { // TODO deal with case that string has escaped characters
            String text = tree.getToken().getText();
            return new IRStringLiteral(text.substring(1, text.length()-1));
        }
		ConcreteTree firstChild = tree.getFirstChild();
		if(firstChild.isNode()) {
			return new IRLenExpression(firstChild.getRightSibling().getToken());
		}
		else if(firstChild.getName().equals("method_call")) {
		    return parseMethodExpression(firstChild);
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
			if (toReturn == null) {
				throw new RuntimeException("Literal " + token.getText() + " could not be parsed.");
			}
			toReturn.setLineNumbers(token);
			return toReturn;
		}
		default:
		    if (tree.isNode()) { // TODO deal with case that string has escaped characters
	            String text = tree.getToken().getText();
	            return new IRStringLiteral(text.substring(1, text.length()-1));
	        }
		    throw new RuntimeException("You tried parsing a badly formatted expression!");
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

    public static IRStatement parseStatement(ConcreteTree tree, VariableTable scope) {
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
            toReturn.setLineNumbers(tree);
            return toReturn;
        }
    
      	String nodeName = child.getName();
      	switch(nodeName) {
      	case "assign_expr":
      	    ConcreteTree locationTree = child.getFirstChild();
      		IRVariableExpression location = parseLocation(locationTree);
      		ConcreteTree operatorTree = locationTree.getRightSibling();
      		Token operator = operatorTree.getToken();
      		ConcreteTree expressionTree = operatorTree.getRightSibling();
      		IRExpression assignment = null;
      		if(expressionTree != null) {
      			assignment = parseExpressionTree(expressionTree);
      		}
      		toReturn = new IRAssignStatement(location, operator, assignment);
      		break;
      	case "method_call":
      		IRExpression methodCall = parseMethodExpression(child);
      		toReturn = new IRMethodCallStatement(methodCall);
      		break;
      	case "if_block":
      	    child = child.getFirstChild();
      		IRExpression ifCondition = parseExpressionTree(child);
      		IRBlock ifBlock = parseBlock(child.getRightSibling(), scope);
      		if(child.getRightSibling().getRightSibling() != null) {
      			IRBlock elseBlock = parseBlock(child.getRightSibling().getRightSibling(), scope);
      			toReturn = new IRIfStatement(ifCondition,  ifBlock, elseBlock);
      		} else {
      			toReturn = new IRIfStatement(ifCondition, ifBlock);
      		}
      		break;
      	case "for_block":{
      	    child = child.getFirstChild();
    		IRAssignStatement initializer = makeForLoopInitializer(child);
    		child = child.getRightSibling().getRightSibling().getRightSibling();
    		IRExpression condition = parseExpressionTree(child);
    		child = child.getRightSibling();
    		IRAssignStatement stepFunction = makeForLoopStepFunction(child);
    		while (!child.getName().equals("block")) {
    			child = child.getRightSibling();
    		}
    		IRBlock block = parseBlock(child, scope);
    		toReturn = new IRForStatement(initializer, condition, stepFunction, block);
    		break;
      	}
      	case "while_block": {
      	    child = child.getFirstChild();
      		IRExpression loopCondition = parseExpressionTree(child);
      		IRBlock block = parseBlock(child.getRightSibling(), scope);
      		toReturn = new IRWhileStatement(loopCondition, block);
      		break;
      	}
      	}
      	toReturn.setLineNumbers(tree);
      	return toReturn;
    }
  
    private static IRAssignStatement makeForLoopInitializer(ConcreteTree child) {
		IRVariableExpression varAssigned = new IRVariableExpression(child.getToken());
		child = child.getRightSibling();
		Token operator = child.getToken();
		child = child.getRightSibling();
		IRExpression value = parseExpressionTree(child);
		IRAssignStatement toReturn = new IRAssignStatement(varAssigned, operator, value);
		toReturn.setLineNumbers(child);
		return toReturn;
    }
  
	private static IRAssignStatement makeForLoopStepFunction(ConcreteTree child) {
		IRVariableExpression varAssigned = IRVariableExpression.makeIRVariableExpression(child);
		child = child.getRightSibling();
		Token operator = child.getToken();
		IRAssignStatement toReturn;
		if (operator.getType() == DecafParserTokenTypes.OP_INC || operator.getType() == DecafParserTokenTypes.OP_DEC) {
			toReturn = new IRAssignStatement(varAssigned, operator, null);
		} else {
			child = child.getRightSibling();
			toReturn = new IRAssignStatement(varAssigned, operator, parseExpressionTree(child));
		}
		toReturn.setLineNumbers(child);
		return toReturn;
	}
  
  public static IRBlock parseBlock(ConcreteTree tree, VariableTable parentScope) {
  		VariableTable fields = new VariableTable(parentScope);
  		ConcreteTree child = tree.getFirstChild();
  		while (child != null && child.getName().equals("field_decl")) {
  			ConcreteTree fieldType = child.getFirstChild();
  			Token typeToken = fieldType.getToken();
  			ConcreteTree fieldName = fieldType.getRightSibling();
  			while (fieldName != null) {
  				Token id = fieldName.getFirstChild().getToken();
  				if (fieldName.getFirstChild() != fieldName.getLastChild()) {
  					Token length = fieldName.getFirstChild().getRightSibling().getRightSibling().getToken();
  					int lengthAsInt = Integer.parseInt(length.getText());
  					fields.add(new IRFieldDecl(IRType.getType(typeToken, lengthAsInt), id, lengthAsInt));
  				} else {
  					fields.add(new IRFieldDecl(IRType.getType(typeToken), id));
  				}
  				fieldName = fieldName.getRightSibling();
  			}
  			child = child.getRightSibling();
  		}
  		
  		List<IRStatement> statements = new ArrayList<>();
  		while (child != null) {
  			statements.add(parseStatement(child, fields));
  			child = child.getRightSibling();
  		}
  		IRBlock block = new IRBlock(statements, fields);
  		block.setLineNumbers(tree);
  		
  		return block;
	}
}

package edu.mit.compilers.trees;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

import antlr.Token;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.IRFieldDecl;
import edu.mit.compilers.ir.decl.IRImportDecl;
import edu.mit.compilers.ir.decl.IRMethodDecl;
import edu.mit.compilers.ir.decl.IRParameterDecl;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.IRBoolLiteral;
import edu.mit.compilers.ir.expression.literal.IRIntLiteral;
import edu.mit.compilers.ir.expression.literal.IRStringLiteral;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.MethodTable;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.symbol_tables.VariableTable;
import edu.mit.compilers.symbol_tables.VariableDescriptor;

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

    public static IRProgram getIRNew(ConcreteTree tree) {
	    simplifyTree(tree);
	    return parseProgram(tree);
	}

    public static IRProgram parseProgram(ConcreteTree tree) {
		List<IRImportDecl> imports = new ArrayList<>();
		VariableTable fields = new VariableTable();
		MethodTable methods = new MethodTable();

		ConcreteTree child = tree.getFirstChild();
		while (child != null && child.getName().equals("import_decl")) {
			IRImportDecl imp = new IRImportDecl(child.getFirstChild().getToken());
			imports.add(imp); // TODO maybe remove
			methods.add(imp);
			child = child.getRightSibling();
		}

		while (child != null && child.getName().equals("field_decl")) {
			ConcreteTree grandchild = child.getFirstChild();
			Token typeToken = grandchild.getToken();
			grandchild = grandchild.getRightSibling();
			while (grandchild != null) {
				Token id = grandchild.getFirstChild().getToken();
				if (grandchild.getFirstChild() != grandchild.getLastChild()) {
					Token length = grandchild.getFirstChild().getRightSibling().getRightSibling().getToken();
					int lengthAsInt = Integer.parseInt(length.getText());
					fields.add(new VariableDescriptor(new IRFieldDecl(TypeDescriptor.getType(typeToken, lengthAsInt), id, lengthAsInt)));
				} else {
					fields.add(new VariableDescriptor(new IRFieldDecl(TypeDescriptor.getType(typeToken), id)));
				}
				grandchild = grandchild.getRightSibling();
			}
			child = child.getRightSibling();
		}

		while (child != null && child.getName().equals("method_decl")) {
			makeIRMethodDecl(child, fields, methods);
			child = child.getRightSibling();
		}

		IRProgram toReturn = new IRProgram(imports, fields, methods);
		toReturn.setLineNumbers(tree);
		return toReturn;
    }

    public static IRMethodCallExpression parseMethodExpression(ConcreteTree tree, VariableTable fields, MethodTable methods) {
        List<IRExpression> arguments = new ArrayList<>();
        String methodName = tree.getFirstChild().getToken().getText();
        ConcreteTree nextChild = tree.getFirstChild().getRightSibling();
        while (nextChild != null) {
            arguments.add(parseExpressionTree(nextChild, fields, methods));
            nextChild = nextChild.getRightSibling();
        }
        IRMethodCallExpression answer = new IRMethodCallExpression(methodName, arguments);
        answer.setTables(fields, methods);
        answer.setLineNumbers(tree);
        IRMethodDecl md = methods.get(methodName);
        answer.setType(md == null ? TypeDescriptor.UNSPECIFIED : md.getReturnType());
        return answer;
    }

    public static IRExpression parseExpressionTree(ConcreteTree tree, VariableTable fields, MethodTable methods) {
        IRExpression toReturn = null;
        if (tree == null) {
            throw new RuntimeException("trying to parse null expression tree");
        }
        String nodeName = tree.getName();
        switch(nodeName) {
            case "expr_0": {
                toReturn = parseExpressionTree(tree.getFirstChild(), fields, methods);
                break;
            }
            case "expr_1": {
                IRExpression returnValue = parseExpressionTree(tree.getLastChild(), fields, methods);
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
                toReturn = returnValue;
                break;
            }
            case "expr_2":
            case "expr_3":
            case "expr_4":
            case "expr_5":
            case "expr_6":
            case "expr_7": {
                ConcreteTree nextNode = tree.getFirstChild();
                IRExpression returnExpression = parseExpressionTree(nextNode, fields, methods);
                while(nextNode.getRightSibling() != null) {
                    ConcreteTree operator = nextNode.getRightSibling();
                    nextNode = operator.getRightSibling();
                    if (nextNode == null) {
                        throw new RuntimeException("This binary operator node does not have odd number of children.");
                    }
                    IRExpression nextTerm = parseExpressionTree(nextNode, fields, methods);
                    returnExpression = new IRBinaryOpExpression(returnExpression, operator.getToken(), nextTerm);
                }
                toReturn = returnExpression;
                break;
            }
            case "expr_8": {
                ConcreteTree nextNode = tree.getLastChild();
                IRExpression returnExpression = parseExpressionTree(nextNode, fields, methods);
                while(nextNode.getLeftSibling() != null) {
                    ConcreteTree trueExpressionTree = nextNode.getLeftSibling();
                    ConcreteTree conditionTree = trueExpressionTree.getLeftSibling();
                    nextNode = conditionTree;
                    IRExpression trueExpression = parseExpressionTree(trueExpressionTree, fields, methods);
                    IRExpression condition = parseExpressionTree(conditionTree, fields, methods);
                    returnExpression = new IRTernaryOpExpression(condition, trueExpression, returnExpression);
                }
                toReturn = returnExpression;
                break;
            }
            case "expr_base": {
                ConcreteTree firstChild = tree.getFirstChild();
                if(firstChild.isNode()) {
                    toReturn = new IRLenExpression(firstChild.getRightSibling().getToken());
                    toReturn.setLineNumbers(firstChild);
                }
                else if(firstChild.getName().equals("method_call")) {
                    toReturn = parseMethodExpression(firstChild, fields, methods);
                }
                else if(firstChild.getName().equals("location")) {
                    toReturn = makeIRVariableExpression(firstChild, fields, methods);
                }
                else if(firstChild.getName().equals("literal")) {
                    ConcreteTree literalValue = firstChild.getFirstChild();
                    Token token = literalValue.getToken();
                    int tokentype = token.getType();

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
                }
                break;
            }
            default:
            if (tree.isNode()) { // TODO deal with case that string has escaped characters
                String text = tree.getToken().getText();
                IRExpression returnExpression = new IRStringLiteral(text.substring(1, text.length()-1));
                returnExpression.setLineNumbers(tree);
                toReturn = returnExpression;
                break;
            }
            throw new RuntimeException("You tried parsing a badly formatted expression!");
        }
        toReturn.setLineNumbers(tree);
        toReturn.setTables(fields, methods);
        return toReturn;
    }

    public static IRStatement parseStatement(ConcreteTree tree, VariableTable scope, MethodTable methods) {
        ConcreteTree child = tree.getFirstChild();
        IRStatement toReturn = null;
      	if (child.isNode()) {
            int tokentype = child.getToken().getType();
            if (tokentype == DecafParserTokenTypes.TK_return) {
            	IRExpression returnExpression = null;
            	if (child.getRightSibling() != null) {
            		returnExpression = parseExpressionTree(child.getRightSibling(), scope, methods);
            	}
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
      	case "assign_expr": {
      	    ConcreteTree locationTree = child.getFirstChild();
      		IRVariableExpression location = makeIRVariableExpression(locationTree, scope, methods);
      		ConcreteTree operatorTree = locationTree.getRightSibling();
      		Token operator = operatorTree.getToken();
      		ConcreteTree expressionTree = operatorTree.getRightSibling();
      		IRExpression assignment = null;
      		if(expressionTree != null) {
      			assignment = parseExpressionTree(expressionTree, scope, methods);
      		}
      		toReturn = new IRAssignStatement(location, operator, assignment);
      		break;
        }
      	case "method_call": {
      		IRExpression methodCall = parseMethodExpression(child, scope, methods);
      		toReturn = new IRMethodCallStatement(methodCall);
      		break;
        }
      	case "if_block": {
      	    child = child.getFirstChild();
      		IRExpression ifCondition = parseExpressionTree(child, scope, methods);
      		IRBlock ifBlock = parseBlock(child.getRightSibling(), scope, methods);
      		if(child.getRightSibling().getRightSibling() != null) {
      			IRBlock elseBlock = parseBlock(child.getRightSibling().getRightSibling(), scope, methods);
      			toReturn = new IRIfStatement(ifCondition, ifBlock, elseBlock);
      		} else {
      			toReturn = new IRIfStatement(ifCondition, ifBlock);
      		}
      		break;
        }
      	case "for_block":{
      	    child = child.getFirstChild();
    		IRAssignStatement initializer = makeForLoopInitializer(child, scope, methods);
    		child = child.getRightSibling().getRightSibling().getRightSibling();
    		IRExpression condition = parseExpressionTree(child, scope, methods);
    		child = child.getRightSibling();
    		IRAssignStatement stepFunction = makeForLoopStepFunction(child, scope, methods);
    		while (!child.getName().equals("block")) {
    			child = child.getRightSibling();
    		}
    		IRBlock block = parseBlock(child, scope, methods);
    		toReturn = new IRForStatement(initializer, condition, stepFunction, block);
    		break;
      	}
      	case "while_block": {
      	    child = child.getFirstChild();
      		IRExpression loopCondition = parseExpressionTree(child, scope, methods);
      		IRBlock block = parseBlock(child.getRightSibling(), scope, methods);
      		toReturn = new IRWhileStatement(loopCondition, block);
      		break;
      	}
      	}
      	toReturn.setLineNumbers(tree);
		toReturn.setTables(scope, methods);
      	return toReturn;
    }

    public static IRBlock parseBlock(ConcreteTree tree, VariableTable parentScope, MethodTable methods) {
		VariableTable fields = new VariableTable(parentScope);
        List<IRFieldDecl> fieldDecls = new ArrayList<IRFieldDecl>();
		ConcreteTree child = tree.getFirstChild();
		while (child != null && child.getName().equals("field_decl")) {
			ConcreteTree fieldType = child.getFirstChild();
			Token typeToken = fieldType.getToken();
			ConcreteTree fieldName = fieldType.getRightSibling();
			while (fieldName != null) {
				Token id = fieldName.getFirstChild().getToken();
                IRFieldDecl field;
				if (fieldName.getFirstChild() != fieldName.getLastChild()) {
					Token length = fieldName.getFirstChild().getRightSibling().getRightSibling().getToken();
					int lengthAsInt = Integer.parseInt(length.getText());
					field = new IRFieldDecl(TypeDescriptor.getType(typeToken, lengthAsInt), id, lengthAsInt);
				} else {
					field = new IRFieldDecl(TypeDescriptor.getType(typeToken), id);
				}
                fieldDecls.add(field);
                fields.add(new VariableDescriptor(field));
				fieldName = fieldName.getRightSibling();
			}
			child = child.getRightSibling();
		}

		List<IRStatement> statements = new ArrayList<>();
		while (child != null) {
			statements.add(parseStatement(child, fields, methods));
			child = child.getRightSibling();
		}
		IRBlock block = new IRBlock(fieldDecls, statements, fields);
		block.setLineNumbers(tree);
		block.setTables(fields, methods);

		return block;
	}

	private static IRAssignStatement makeForLoopInitializer(ConcreteTree child, VariableTable fields, MethodTable methods) {
		IRVariableExpression varAssigned = new IRVariableExpression(child.getToken());
		child = child.getRightSibling();
		Token operator = child.getToken();
		child = child.getRightSibling();
		IRExpression value = parseExpressionTree(child, fields, methods);
		IRAssignStatement toReturn = new IRAssignStatement(varAssigned, operator, value);

		toReturn.setLineNumbers(child);
		toReturn.setTables(fields, methods);

		return toReturn;
  }

	private static IRAssignStatement makeForLoopStepFunction(ConcreteTree child, VariableTable fields, MethodTable methods) {
		IRVariableExpression varAssigned = makeIRVariableExpression(child, fields, methods);
		child = child.getRightSibling();
		Token operator = child.getToken();
		IRAssignStatement toReturn;
		if (operator.getType() == DecafParserTokenTypes.OP_INC || operator.getType() == DecafParserTokenTypes.OP_DEC) {
			toReturn = new IRAssignStatement(varAssigned, operator, null);
		} else {
			child = child.getRightSibling();
			toReturn = new IRAssignStatement(varAssigned, operator, parseExpressionTree(child, fields, methods));
		}
		toReturn.setTables(fields, methods);
		toReturn.setLineNumbers(child);
		return toReturn;
	}

	private static IRVariableExpression makeIRVariableExpression(ConcreteTree tree, VariableTable fields, MethodTable methods) {
		if (tree == null) {
			System.err.println("ERROR: null tree in IRVariableExpression.makeIRVariableExpression.");
		}
		ConcreteTree child = tree.getFirstChild();
		String name = child.getToken().getText();
		child = child.getRightSibling();
		IRVariableExpression toReturn;
        VariableDescriptor desc = fields.get(name);
        TypeDescriptor varType = (desc == null) ? TypeDescriptor.UNSPECIFIED : desc.getType();
                if (child == null) {
			toReturn = new IRVariableExpression(name);
		} else {
			child = child.getRightSibling();
			toReturn = new IRVariableExpression(name, parseExpressionTree(child, fields, methods));
			if(varType.isArray()) {
				varType = varType.getArrayElementType();
			}
		}

		toReturn.setType(varType);
		toReturn.setLineNumbers(tree);
		toReturn.setTables(fields, methods);
		return toReturn;
	}

	private static IRMethodDecl makeIRMethodDecl(ConcreteTree tree, VariableTable parentScope, MethodTable methods) {
	    VariableTable parameters = new VariableTable(parentScope);
	    ConcreteTree child = tree.getFirstChild();
	    TypeDescriptor returnType = TypeDescriptor.getType(child.getToken());
	    
	    child = child.getRightSibling();
	    Token id = child.getToken();
	    child = child.getRightSibling();
	    while(child.isNode()) {
	        TypeDescriptor parameterType = TypeDescriptor.getType(child.getToken());
	        child = child.getRightSibling();
	        Token parameterId = child.getToken();
	        parameters.add(new VariableDescriptor(new IRParameterDecl(parameterType, parameterId)));
	        child = child.getRightSibling();
	    }
	    IRMethodDecl decl = new IRMethodDecl(returnType, id, parameters, null);
	    decl.setLineNumbers(tree);
	    decl.setTables(parentScope, methods);
	    methods.add(decl);
	    
	    IRBlock code = parseBlock(child, parameters, methods);
	    decl.setCode(code);

	    return decl;
	  }
}

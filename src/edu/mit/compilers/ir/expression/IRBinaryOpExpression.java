package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.operator.IRBinaryOperator;
import edu.mit.compilers.trees.ConcreteTree;

public class IRBinaryOpExpression extends IRExpression{
	private IRExpression leftExpr;
	private IRExpression rightExpr;

	//private IRBinaryOperator operator;
	private Token operator;

	public IRBinaryOpExpression(IRExpression leftExpr, Token operator, IRExpression rightExpr) {
		setLineNumbers(leftExpr);
		expressionType = IRExpression.ExpressionType.BINARY;
		this.leftExpr = leftExpr;
		this.operator = operator;
		this.rightExpr = rightExpr;
	}

	public IRBinaryOpExpression(ConcreteTree tree) {
		expressionType = IRExpression.ExpressionType.BINARY;
		setLineNumbers(tree);
		ConcreteTree exprChild = tree.getFirstChild();
		IRExpression lexpr = IRExpression.makeIRExpression(exprChild);
		ConcreteTree opChild = exprChild.getRightSibling();
		exprChild = opChild.getRightSibling();
		ConcreteTree lastExprChild = tree.getLastChild();
		while (exprChild != lastExprChild) {
			IRExpression rexpr = IRExpression.makeIRExpression(exprChild);
			lexpr = new IRBinaryOpExpression(lexpr, opChild.getToken(), rexpr);
			opChild = exprChild.getRightSibling();
			exprChild = opChild.getRightSibling();
		}
		leftExpr = lexpr;
		operator = opChild.getToken();
		rightExpr = makeIRExpression(exprChild);
	}

	public IRExpression getLeftExpr() { return leftExpr; }
	public IRExpression getRightExpr() { return rightExpr; }
	public Token getOperator() { return operator; }

	@Override
	public IRType.Type getType() {
		// TODO dear god refactor this
		String op = operator.getText();
		if (op.equals("==") || op.equals("!=") || op.equals("&&") || op.equals("||") || op.equals("<")
            || op.equals("<=") || op.equals(">") || op.equals(">=")) {
			return IRType.Type.BOOL;
		}
		else if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") || op.equals("%") ) {
			return IRType.Type.INT;
		}
		else {
			throw new RuntimeException("Undefined operator " + op + ".");
		}
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(leftExpr, rightExpr);
	}

	@Override
	public String toString() { //TODO remove if null
		return (leftExpr == null ? "var" : leftExpr.toString()) + " " + operator.getText() + " " + (rightExpr == null ? "var" : rightExpr.toString());
	}

}

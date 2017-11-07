package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class IRUnaryOpExpression extends IRExpression {

	//private IRUnaryOperator operator;
	private Token operator;
	private IRExpression argument;

	public IRUnaryOpExpression(Token operator, IRExpression argument) {
		setLineNumbers(operator);
		expressionType = IRExpression.ExpressionType.UNARY;
		this.operator = operator;
		this.argument = argument;
	}

	public Token getOperator() { return operator; }
	public IRExpression getArgument() { return argument; }

	@Override
	public TypeDescriptor getType() {
		// TODO dear god refactor this
		String op = operator.getText();
		if (op.equals("!")) {
			return TypeDescriptor.BOOL;
		}
		else if (op.equals("-")) {
			return TypeDescriptor.INT;
		}
		else {
			throw new RuntimeException("Undefined operator " + op + ".");
		}
	}

	@Override
	public List<IRExpression> getChildren() {
		return Arrays.asList(argument);
	}

	@Override
	public String toString() { //TODO remove if null
		return operator.getText() + ((argument == null) ? "var" : argument);
	}
	
	@Override
	public int getDepth() {
		return argument.getDepth() + 1;
	}
	
	@Override
	public <R> R accept(IRExpressionVisitor<R> visitor) {
		return visitor.on(this);
	}

	@Override
	public <R> R accept(IRNodeVisitor<R> visitor) {
		return visitor.on(this);
	}

}

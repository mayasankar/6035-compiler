package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class IRUnaryOpExpression extends IRExpression {

	private String operator;
	private IRExpression argument;

	public IRUnaryOpExpression(Token operator, IRExpression argument) {
		//setLineNumbers(operator); //TODO mayars why is this gone?
		expressionType = IRExpression.ExpressionType.UNARY;
		this.operator = operator.getText();
		this.argument = argument;
	}

	public IRUnaryOpExpression(String operator, IRExpression argument) {
		expressionType = IRExpression.ExpressionType.UNARY;
		this.operator = operator;
		this.argument = argument;
	}

    private IRUnaryOpExpression(IRUnaryOpExpression other) {
        expressionType = IRExpression.ExpressionType.UNARY;
        this.operator = other.operator;
        this.argument = other.argument.copy();
    }

    @Override
    public IRUnaryOpExpression copy() { return new IRUnaryOpExpression(this); }

	public String getOperator() { return operator; }
	public IRExpression getArgument() { return argument; }

	@Override
	public TypeDescriptor getType() {
		// TODO dear god refactor this
		if (operator.equals("!")) {
			return TypeDescriptor.BOOL;
		}
		else if (operator.equals("-")) {
			return TypeDescriptor.INT;
		}
		else {
			throw new RuntimeException("Undefined operator " + operator + ".");
		}
	}

	@Override
	public List<IRExpression> getChildren() {
		return Arrays.asList(argument);
	}

	@Override
	public String toString() { //TODO remove if null
		return operator + ((argument == null) ? "var" : argument);
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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IRUnaryOpExpression) {
			IRUnaryOpExpression expr = (IRUnaryOpExpression)obj;
			return (this.argument.equals(expr.argument) && this.operator.equals(expr.operator));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.argument.hashCode() + 17*this.operator.hashCode();
	}

}

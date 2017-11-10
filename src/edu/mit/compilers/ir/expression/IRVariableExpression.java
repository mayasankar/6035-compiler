package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class IRVariableExpression extends IRExpression implements IRExpression.Renameable {

	private String variableName;
	private IRExpression arrayIndex = null;
	private TypeDescriptor type = TypeDescriptor.UNSPECIFIED; // will never be INT_ARRAY or BOOL_ARRAY if semantically valid


	public IRVariableExpression(Token id) {
		expressionType = IRExpression.ExpressionType.VARIABLE;
		variableName = id.getText();
	}

	public IRVariableExpression(String variableName) {
		expressionType = IRExpression.ExpressionType.VARIABLE;
		this.variableName = variableName;
		// TODO: make a variable or look one up here
	}

	public IRVariableExpression(String variableName, IRExpression expression) {
		expressionType = IRExpression.ExpressionType.VARIABLE;
		this.variableName = variableName;
		arrayIndex = expression;
	}

	public String getName() { return variableName; }
	public IRExpression getIndexExpression() { return arrayIndex; }

    @Override
	public void resetName(String newName) { variableName = newName; }

	@Override
	public TypeDescriptor getType() {
		return type;
	}

	public void setType(TypeDescriptor t) {
		type = t;
	}

	public boolean isArray() {
		return arrayIndex != null;
	}

	@Override
	public List<IRExpression> getChildren() {
		return Arrays.asList();
	}

	@Override
	public String toString() {
		if (arrayIndex == null) {
			return variableName;
		} else {
			return variableName + '[' + arrayIndex + ']';
		}
	}

	@Override
	public int getDepth() {
		return 0;
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

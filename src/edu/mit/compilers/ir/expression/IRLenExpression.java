package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.IRNode.IRNodeVisitor;
import edu.mit.compilers.ir.expression.IRExpression.IRExpressionVisitor;
import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class IRLenExpression extends IRExpression implements IRExpression.Renameable {
    private String variableName; // what you are taking the length of

    public IRLenExpression(Token id) { this(id.getText()); }

    private IRLenExpression(String varName) {
        expressionType = IRExpression.ExpressionType.LEN;
        variableName = varName;
    }

    @Override
    public IRLenExpression copy() { return new IRLenExpression(variableName); }

    public String getArgument() { return variableName; }
    @Override
    public void resetName(String newName) { variableName = newName; }

    @Override
    public String toString() { return "len(" + variableName + ")"; }

    @Override
    public TypeDescriptor getType() {
        return TypeDescriptor.INT;
    }

	@Override
	public int getDepth() {
		return 1;
	}

    @Override
    public List<IRExpression> getChildren() {
        // TODO Auto-generated method stub
        return Arrays.asList();
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
        if (obj instanceof IRLenExpression) {
            IRLenExpression expr = (IRLenExpression)obj;
            return (this.variableName.equals(expr.variableName));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.variableName.hashCode();
    }

    @Override
    public boolean isConstant() { return true; }
}

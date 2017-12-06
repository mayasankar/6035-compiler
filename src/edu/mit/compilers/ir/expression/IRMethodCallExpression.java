package edu.mit.compilers.ir.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class IRMethodCallExpression extends IRExpression {

	private TypeDescriptor type = TypeDescriptor.UNSPECIFIED;
	private final List<IRExpression> arguments;
	private String functionName;

	public IRMethodCallExpression(String descriptorName, List<IRExpression> arguments) {
        // TODO -- line numbers?
		expressionType = IRExpression.ExpressionType.METHOD_CALL;
		this.arguments = Collections.unmodifiableList(arguments);
		this.functionName = descriptorName;
		//TODO: some code to get the method descriptor
	}

    private IRMethodCallExpression(IRMethodCallExpression other) {
        List<IRExpression> myArgs = new ArrayList<>();
        for (IRExpression expr : other.arguments) { myArgs.add(expr.copy()); }
        this.arguments = Collections.unmodifiableList(myArgs);
        this.functionName = other.functionName;
    }

    @Override
    public IRMethodCallExpression copy() { return new IRMethodCallExpression(this); }

	@Override
	public TypeDescriptor getType() {
		return type;
	}

	public List<IRExpression> getArguments() { return this.arguments; }
	public String getName() { return this.functionName; }

	public void setType(TypeDescriptor t){
		type = t;
	}

    // TODO for optimization, implement this better
    // this allows DCE to delete calls to this method.
    public boolean affectsGlobals() { return true; }

    // TODO for CP optimization, improve this
    public boolean isConstant() { return false; }

	@Override
	public List<IRExpression> getChildren() {
		return arguments;
	}

	@Override
	public String toString() {
		String answer = functionName + "(";
		for (IRExpression arg : arguments) {
			answer += (arg == null ? "null" : arg.toString()) + ", ";
		}
		answer += ")";
		return answer;
	}

	@Override
	public int getDepth() {
		int maxArgDepth = 0;
		for(IRExpression arg: arguments) {
			maxArgDepth = Math.max(maxArgDepth, arg.getDepth());
		}
		return maxArgDepth + 1;
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
        if (obj instanceof IRMethodCallExpression) {
            IRMethodCallExpression expr = (IRMethodCallExpression) obj;
            return (expr.functionName.equals(this.functionName)) && (expr.type == this.type)
                    && (expr.arguments.equals(this.arguments));
        }
        return false;
    }

}

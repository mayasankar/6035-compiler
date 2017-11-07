package edu.mit.compilers.ir.expression;

import java.util.Collections;
import java.util.List;

import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class IRMethodCallExpression extends IRExpression {

	private TypeDescriptor type = TypeDescriptor.UNSPECIFIED;
	private final List<IRExpression> arguments;
	private String functionName;

	public IRMethodCallExpression(String descriptorName, List<IRExpression> arguments) {
		expressionType = IRExpression.ExpressionType.METHOD_CALL;
		this.arguments = Collections.unmodifiableList(arguments);
		this.functionName = descriptorName;
		//TODO: some code to get the method descriptor
	}

	@Override
	public TypeDescriptor getType() {
		return type;
	}

	public List<IRExpression> getArguments() { return this.arguments; }
	public String getName() { return this.functionName; }

	public void setType(TypeDescriptor t){
		type = t;
	}

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

}

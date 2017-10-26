package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.ir.decl.IRMethodDecl;

public class IRMethodCallExpression extends IRExpression {

	private IRType.Type type = null;
	private final List<IRExpression> arguments;
	private String functionName;

	public IRMethodCallExpression(String descriptorName, List<IRExpression> arguments) {
		expressionType = IRExpression.ExpressionType.METHOD_CALL;
		this.arguments = Collections.unmodifiableList(arguments);
		this.functionName = descriptorName;
		//TODO: some code to get the method descriptor
	}

	@Override
	public IRType.Type getType() {
		return type;
	}

	public List<IRExpression> getArguments() { return this.arguments; }
	public String getName() { return this.functionName; }

	public void setType(IRType.Type t){
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

}

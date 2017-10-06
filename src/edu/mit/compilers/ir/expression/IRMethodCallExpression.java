package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.decl.IRMethodDecl;

public class IRMethodCallExpression extends IRExpression {

	private IRMethodDecl descriptor;

	private final List<IRExpression> arguments;

	public IRMethodCallExpression(String descriptorName, IRExpression... arguments) {
		this.arguments = Collections.unmodifiableList(Arrays.asList(arguments));
		//TODO: some code to get the method descriptor

	}

	@Override
	public IRType.Type getType() {
		return descriptor.getReturnType();
	}

	public IRMethodDecl getIRMethodDecl() { return this.descriptor; }
	public List<IRExpression> getArguments() { return this.arguments; }

	public void setDescriptor(IRMethodDecl descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return arguments;
	}

}

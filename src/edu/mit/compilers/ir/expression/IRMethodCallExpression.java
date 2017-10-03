package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.symbol_tables.MethodDescriptor;

public class IRMethodCallExpression extends IRExpression {
	
	private MethodDescriptor descriptor;
	
	private final List<IRExpression> arguments;
	
	public IRMethodCallExpression(String descriptorName, IRExpression... arguments) {
		this.arguments = Collections.unmodifiableList(Arrays.asList(arguments));
		//TODO: some code to get the method descriptor
		
	}
	
	@Override
	public IRType getType() {
		return IRType.getTypeFromDescriptor(descriptor.getReturnType());
	}
	
	public void setDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return arguments;
	}

}

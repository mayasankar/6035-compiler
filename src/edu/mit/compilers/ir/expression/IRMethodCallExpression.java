package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.symbol_tables.MethodDescriptor;
import edu.mit.compilers.trees.ConcreteTree;

public class IRMethodCallExpression extends IRExpression {

	private MethodDescriptor descriptor;

	private List<IRExpression> arguments;

	private String functionName; // TODO maybe not actually necessary

	public IRMethodCallExpression(String descriptorName, IRExpression... arguments) {
		this.arguments = Collections.unmodifiableList(Arrays.asList(arguments));
		//TODO: some code to get the method descriptor
	}

	public IRMethodCallExpression(ConcreteTree tree) {
		ConcreteTree child = tree.getFirstChild();
		functionName = child.getToken().getText();
		arguments = new ArrayList<IRExpression>();
		while (child != null) {
			arguments.add(IRExpression.makeIRExpression(child));
			child = child.getRightSibling();
		}
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

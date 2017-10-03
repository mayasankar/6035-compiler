package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.symbol_tables.Variable;

public class IRVariableExpression extends IRExpression {
	
	private Variable variable;
	
	public IRVariableExpression(String variableName) {
		// TODO: make a variable or look one up here
	}
	
	@Override
	public IRType getType() {
		return  null;//IRType.getTypeFromDescriptor(variable.getType()); TODO: This will complain until Variables have the right type
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList();
	}

}

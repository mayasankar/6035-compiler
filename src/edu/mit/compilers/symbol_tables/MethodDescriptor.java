package edu.mit.compilers.symbol_tables;

import edu.mit.compilers.symbol_tables.VariableTable;
import edu.mit.compilers.symbol_tables.Descriptor;
import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class MethodDescriptor extends Descriptor {
	protected TypeDescriptor returnType;
	protected int codeLocation;  // TODO should this be stored elsehow?
	protected VariableTable methodVariables;

	MethodDescriptor(String name, TypeDescriptor type, int location, VariableTable variables){
		super(name);
		this.returnType = type;
		this.codeLocation = location;
		this.methodVariables = variables;
	}

	public TypeDescriptor getReturnType() {
		return returnType;
	}

	public int getCodeLocation() {
		return codeLocation;
	}

	public VariableTable getMethodVariables() {
		return methodVariables;
	}

}

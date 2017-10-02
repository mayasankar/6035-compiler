package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.Collections;
import edu.mit.compilers.symbol_tables.Descriptor;

public class ClassDescriptor extends Descriptor {
	protected ClassDescriptor parentClass;
	protected int codeLocation;  // TODO should this be stored elsehow?
	protected List<String> methodVariables;  // TODO we should keep things other than name ? like type ?

	Variable(String name, TypeDescriptor type, int location, List<String> variables){
		super(name);
		this.returnType = type;
		this.codeLocation = location;
		this.methodVariables = variables;
	}

	public ClassDescriptor getParent() {
		return parentClass;
	}

	public int getCodeLocation() {
		return codeLocation;
	}

	public List<String> getMethodVariables() {
		return Collections.unmodifiableList(methodVariables);
	}

}
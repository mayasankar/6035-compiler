package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.Collections;
import edu.mit.compilers.symbol_tables.Descriptor;
import edu.mit.compilers.symbol_tables.MethodTable;
import edu.mit.compilers.symbol_tables.VariablesScope;

public class ClassDescriptor extends Descriptor {
	protected ClassDescriptor parentClass;
	protected MethodTable methods;  // TODO should this be stored elsehow?
	protected VariablesScope fields;  // TODO we should keep things other than name ? like type ?

	Variable(String name, ClassDescriptor parent){
		super(name);
		this.parentClass = parent;
		this.methods = new MethodTable(parent.getMethodTable());
		this.fields = new VariablesScope(parent.getFields());
	}

	public ClassDescriptor getParent() {
		return parentClass;
	}

	public MethodTable getMethodTable() {
		return methods;
	}

	public VariablesScope getFields() {
		return fields;
	}

}
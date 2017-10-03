package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.Collections;
import edu.mit.compilers.symbol_tables.Descriptor;
import edu.mit.compilers.symbol_tables.MethodTable;
import edu.mit.compilers.symbol_tables.VariableTable;

public class ClassDescriptor extends Descriptor {
	protected ClassDescriptor parentClass;
	protected MethodTable methods;  // TODO should this be stored elsehow?
	protected VariableTable fields;  // TODO we should keep things other than name ? like type ?

	ClassDescriptor(String name, ClassDescriptor parent){
		super(name);
		this.parentClass = parent;
		this.methods = new MethodTable(parentClass.getMethodTable());
		this.fields = new VariableTable(parentClass.getFields());
	}

	ClassDescriptor(String name){
		super(name);
		this.parentClass = null;
		this.methods = new MethodTable(parentClass.getMethodTable());
		this.fields = new VariableTable(parentClass.getFields());
	}

	public ClassDescriptor getParent() {
		return parentClass;
	}

	public MethodTable getMethodTable() {
		return methods;
	}

	public VariableTable getFields() {
		return fields;
	}

	public void addMethod(MethodDescriptor m){
		this.methods.add(m);
	}

	public void addField(Variable v){
		this.fields.add(v);
	}

}

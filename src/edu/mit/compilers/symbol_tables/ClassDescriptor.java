package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.Collections;
import edu.mit.compilers.symbol_tables.Descriptor;
import edu.mit.compilers.symbol_tables.MethodTable;
import edu.mit.compilers.symbol_tables.VariableTable;
import edu.mit.compilers.ir.decl.IRFieldDecl;
import edu.mit.compilers.ir.decl.IRMethodDecl;

public class ClassDescriptor extends Descriptor {
	protected ClassDescriptor parentClass;
	protected MethodTable methods;  // TODO should this be stored elsehow?
	protected VariableTable fields;  // TODO we should keep things other than name ? like type ?

	public ClassDescriptor(String name, ClassDescriptor parent){
		super(name);
		this.parentClass = parent;
		this.methods = new MethodTable(parentClass.getMethodTable());
		this.fields = new VariableTable(parentClass.getFields());
	}

	public ClassDescriptor(String name){
		super(name);
		this.parentClass = null;
		this.methods = new MethodTable();
		this.fields = new VariableTable();
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

	public void addMethod(IRMethodDecl m){
		this.methods.add(m);
	}

	public void addField(IRFieldDecl f){
		this.fields.add(f);
	}

}

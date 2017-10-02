package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MethodTable {
	protected MethodTable parent;
	protected Map<String, MethodDescriptor> methods; // lookup method by string name

	MethodTable(MethodTable parent){
		this.parent = parent;
		this.methods = new HashMap<>();
	}

	VariablesScope(){
		this.parent = null;
		this.methods = new HashMap<>();
	}

	public MethodTable getParent() {
		return parent;
	}

	public void add(MethodDescriptor v){
		methods.put(v.getName(), v);
	}

	public MethodDescriptor get(String name){
		if (methods.containsKey(name)){
			return methods.get(name);
		}
		if (parent != null) {
			return parent.get(name);
		}
		return null;
	}

}
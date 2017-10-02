package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class TypeTable {
	protected TypeTable parent;
	protected Map<String, TypeDescriptor> types; // lookup type by string name

	TypeTable(TypeTable parent){
		this.parent = parent;
		this.types = new HashMap<>();
	}

	TypeTable(){
		this.parent = null;
		this.types = new HashMap<>();
	}

	public TypeTable getParent() {
		return parent;
	}

	public void add(TypeDescriptor v){
		types.put(v.getName(), v);
	}

	public TypeDescriptor get(String name){
		if (types.containsKey(name)){
			return types.get(name);
		}
		if (parent != null) {
			return parent.get(name);
		}
		return null;
	}

}
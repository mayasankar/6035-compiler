package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import edu.mit.compilers.symbol_tables.Descriptor;

public abstract class SymbolTable {
	protected SymbolTable parent;
	protected Map<String, Descriptor> lookupObjects; // lookup method by string name

	public SymbolTable(SymbolTable parent){
		this.parent = parent;
		this.lookupObjects = new HashMap<>();
	}

	public SymbolTable(){
		this.parent = null;
		this.lookupObjects = new HashMap<>();
	}

	public SymbolTable getParent() {
		return parent;
	}

	public void add(Descriptor objectToAdd){
		lookupObjects.put(objectToAdd.getName(), objectToAdd);
	}

	public Descriptor get(String objectToGet){
		if (lookupObjects.containsKey(objectToGet)){
			return lookupObjects.get(objectToGet);
		}
		if (parent != null) {
			return parent.get(objectToGet);
		}
		return null;
	}

}

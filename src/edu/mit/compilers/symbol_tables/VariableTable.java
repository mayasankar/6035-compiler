package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import edu.mit.compilers.symbol_tables.Variable;

public class VariableTable {
	protected VariableTable parent;
	protected Map<String, Variable> variables; // lookup variable by string name

	VariableTable(VariableTable parent){
		this.parent = parent;
		this.variables = new HashMap<>();
	}

	VariableTable(){
		this.parent = null;
		this.variables = new HashMap<>();
	}

	public VariableTable getParent() {
		return parent;
	}

	public void add(Variable v){
		variables.put(v.getName(), v);
	}

	public Variable get(String name){
		if (variables.containsKey(name)){
			return variables.get(name);
		}
		if (parent != null) {
			return parent.get(name);
		}
		return null;
	}

}

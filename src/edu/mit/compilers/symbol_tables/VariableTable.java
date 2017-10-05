package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import edu.mit.compilers.ir.decl.IRFieldDecl;

public class VariableTable {
	protected VariableTable parent;
	protected Map<String, IRFieldDecl> variables; // lookup variable by string name

	public VariableTable(VariableTable parent){
		this.parent = parent;
		this.variables = new HashMap<>();
	}

	public VariableTable(){
		this.parent = null;
		this.variables = new HashMap<>();
	}

	public VariableTable getParent() {
		return parent;
	}

	public void add(IRFieldDecl v){
		variables.put(v.getName(), v);
	}

	public IRFieldDecl get(String name){
		if (variables.containsKey(name)){
			return variables.get(name);
		}
		if (parent != null) {
			return parent.get(name);
		}
		return null;
	}

	@Override
	public String toString() {
		String answer = "Variables: \n";
		if (variables.values().size() == 0) {
			answer += "none";
		} else {
			for (IRFieldDecl field : variables.values()) {
				answer += field.toString() + ", ";
			}
		}
		return answer;
	}

}

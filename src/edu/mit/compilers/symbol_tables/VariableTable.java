package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import edu.mit.compilers.ir.decl.IRMemberDecl;

public class VariableTable {
	protected VariableTable parent;
	protected Map<String, IRMemberDecl> variables; // lookup variable by string name

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

	public void add(IRMemberDecl v){
		variables.put(v.getName(), v);
	}

	public IRMemberDecl get(String name){
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
		return toString("Variables");
	}

	public String toString(String var_setting) {
		String answer = var_setting + ": ";
		if (variables.values().size() == 0) {
			answer += "none";
		} else {
			for (IRMemberDecl var : variables.values()) {
				answer += var.toString() + ", ";
			}
		}
		return answer;
	}

}

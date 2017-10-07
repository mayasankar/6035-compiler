package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import edu.mit.compilers.ir.decl.IRMemberDecl;

public class VariableTable {
	protected VariableTable parent;
	protected List<IRMemberDecl> orderedVariables; // allows checking signature
	protected Map<String, IRMemberDecl> variables; // lookup variable by string name

	public VariableTable(VariableTable parent){
		this.parent = parent;
		this.orderedVariables = new ArrayList<>();
		this.variables = new HashMap<>();
	}

	public VariableTable(){
		this.parent = null;
		this.orderedVariables = new ArrayList<>();
		this.variables = new HashMap<>();
	}

	public VariableTable getParent() {
		return parent;
	}

	public void add(IRMemberDecl v) {
		variables.put(v.getName(), v);
		orderedVariables.add(v);
	}

	public List<IRMemberDecl> getVariableList() {
		return orderedVariables;
	}

	public boolean isEmpty() {
		return variables.isEmpty();
	}

	public IRMemberDecl get(String name){
		//System.out.println("Possible values at this scope: " + variables.keySet().toString());
		if (variables.containsKey(name)){
			//System.out.println("Found " + name);
			return variables.get(name);
		}
		if (parent != null) {
			//System.out.println("Recursing for finding " + name);
			return parent.get(name);
		}
		//System.out.println("Didn't find " + name );
		return null;
	}

	@Override
	public String toString() {
		return toString("Variables");
	}

	public String toString(String var_setting) {
		String answer = var_setting + ": ";
		if (orderedVariables.size() == 0) {
			answer += "none";
		} else {
			for (IRMemberDecl var : orderedVariables) {
				answer += var.toString() + ", ";
			}
		}
		return answer;
	}

}

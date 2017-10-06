package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import edu.mit.compilers.ir.decl.IRMethodDecl;

public class MethodTable {
	protected MethodTable parent;
	protected Map<String, IRMethodDecl> methods; // lookup Method by string name

	public MethodTable(MethodTable parent){
		this.parent = parent;
		this.methods = new HashMap<>();
	}

	public MethodTable(){
		this.parent = null;
		this.methods = new HashMap<>();
	}

	public MethodTable getParent() {
		return parent;
	}

	public void add(IRMethodDecl v){
		methods.put(v.getName(), v);
	}

	public IRMethodDecl get(String name){
		if (methods.containsKey(name)){
			return methods.get(name);
		}
		if (parent != null) {
			return parent.get(name);
		}
		return null;
	}

	@Override
	public String toString() {
		String answer = "Methods: \n";
		for (IRMethodDecl method : methods.values()) {
			answer += method.toString() + "\n";
		}
		return answer;
	}

}

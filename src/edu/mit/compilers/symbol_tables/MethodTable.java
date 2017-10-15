package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import edu.mit.compilers.ir.decl.IRMethodDecl;

public class MethodTable extends SymbolTable<MethodTable, IRMethodDecl> {
	
	public MethodTable() {
		super();
	}
	
	public MethodTable(MethodTable parent) {
		super(parent);
	}
	
	public List<IRMethodDecl> getMethodList() {
		return orderedChildren;
	}

	@Override
	public String toString() {
		String answer = "Methods: \n";
		for (IRMethodDecl method : orderedChildren) {
			answer += method.toString() + "\n";
		}
		return answer;
	}

}

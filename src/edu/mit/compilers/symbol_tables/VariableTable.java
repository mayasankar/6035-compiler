package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import edu.mit.compilers.ir.decl.*;

public class VariableTable extends SymbolTable<VariableTable, VariableDescriptor>{

	public VariableTable() {
		super();
	}

	public VariableTable(VariableTable parent) {
		super(parent);
	}

	public List<IRMemberDecl> getVariableList() {
		ArrayList<IRMemberDecl> answer = new ArrayList<>();
		for (VariableDescriptor desc : orderedChildren) {
			answer.add(desc.getDecl());
		}
		return answer;
	}

	@Override
	public String toString() {
		return toString("Variables");
	}

	public String toString(String var_setting) {
		String answer = var_setting + ": ";
		if (orderedChildren.size() == 0) {
			answer += "none";
		} else {
			for (VariableDescriptor var : orderedChildren) {
				answer += var.toString() + ", ";
			}
		}
		return answer;
	}

}

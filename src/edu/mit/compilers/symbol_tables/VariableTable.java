package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import edu.mit.compilers.ir.decl.*;

public class VariableTable extends SymbolTable<VariableTable, VariableDescriptor> {
	private int stackPointer;

	public VariableTable() {
		super();
		stackPointer = 0; // TODO (mayars) do we want to initialize it to 0 or 8?
	}

	public VariableTable(VariableTable parent) {
		super(parent);
		stackPointer = parent.stackPointer;
	}

	public List<IRMemberDecl> getVariableList() {
		ArrayList<IRMemberDecl> answer = new ArrayList<>();
		for (VariableDescriptor desc : orderedChildren) {
			answer.add(desc.getDecl());
		}
		return answer;
	}

	 public List<VariableDescriptor> getVariableDescriptorList() {
         return orderedChildren;
	 }

	 public int getStackOffset(String name) {
	         VariableDescriptor var = this.get(name);
			 if (var == null) {
				 String error = "attempted to get stack offset of undeclared variable: " + name;
				 error += "\nown variables: ";
				 for (VariableDescriptor v: orderedChildren) {
					 error += "\n" + v.getName();
				 }
				 if (this.parent == null) {
					 error += "\nnull parent";
				 }
				 else {
					 error += "\nparent variables:";
					 for (VariableDescriptor v: this.parent.getVariableDescriptorList()) {
						 error += "\n" + v.getName();
					 }
				 }
				 throw new RuntimeException(error);
			 }
	         return var.getStackOffset();
	 }

	@Override
	protected void processDescriptor(VariableDescriptor desc) {
		stackPointer = desc.pushOntoStack(stackPointer);
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

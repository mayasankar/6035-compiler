package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public abstract class VariablesScope {
	protected VariablesScope parent;
	protected Map<String, Variable> variables = new HashMap<>(); // lookup variable by string name

	public Fields getParent() {
		return parent;
	}



}
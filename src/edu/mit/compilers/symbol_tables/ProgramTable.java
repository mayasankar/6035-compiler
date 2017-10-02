package edu.mit.compilers.symbol_tables;

import java.util.List;
import edu.mit.compilers.symbol_tables.VariablesScope;

public class ProgramTable {
	protected VariablesScope fieldTable;
	protected List<String> imports;

	public VariablesScope getFields() {
		return fieldTable;
	}

	public void addField(Field f) {
		Fields.add(f);
	}
}
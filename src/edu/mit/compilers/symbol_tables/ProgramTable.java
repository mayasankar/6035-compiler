package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.Collections;
import edu.mit.compilers.symbol_tables.ClassDescriptor;


public class ProgramTable extends ClassDescriptor {
	protected List<String> imports;

	ProgramTable(){
		String name = "__PROGRAM_TABLE__";
		super(name);
		imports = new List<String>();
	}

	public List<String> getImports() {
		return this.imports;
	}

	public void addImport(String i){
		imports.add(i);
	}

	// has field, method tables just like a Class

}
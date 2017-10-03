package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import edu.mit.compilers.symbol_tables.ClassDescriptor;


public class ProgramDescriptor extends ClassDescriptor {
	protected List<String> imports;

	ProgramDescriptor(){
		super("__PROGRAM_TABLE__");
		imports = new ArrayList<String>(); // List can't be instantiated, it's abstract
	}

	public List<String> getImports() {
		return this.imports;
	}

	public void addImport(String i){
		imports.add(i);
	}

	// has field, method tables just like a Class

}

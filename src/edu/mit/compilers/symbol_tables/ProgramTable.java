package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.ArrayList;

import antlr.Token;
import java.util.Collections;
import edu.mit.compilers.symbol_tables.ClassTable;


public class ProgramTable extends ClassTable {
	protected List<Token> imports;

	public ProgramTable(){
		super("__PROGRAM_TABLE__");
		imports = new ArrayList<Token>();
	}

	public List<Token> getImports() {
		return this.imports;
	}

	public void addImport(Token i){
		imports.add(i);
	}

	// has field, method tables just like a Class

	@Override
	public String toString() {
		String answer = "Imports: ";
		for (Token imp : this.getImports()) {
			answer += imp.getText() + ", ";
		}
		answer += "\n" + this.getFields().toString();
		answer += "\n" + this.getMethodTable().toString();
		return answer;
	}

}

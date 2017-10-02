package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import edu.mit.compilers.symbol_tables.SymbolTable;

public class TypeTable extends SymbolTable {

	TypeTable(TypeTable parent){
		super(parent);
	}

	TypeTable(){
		super();
	}

}
package edu.mit.compilers.symbol_tables;

import java.util.List;


public class Variable {
	protected String name;
	protected String type;  // TODO this should maybe be a TypeDescriptor object
	protected int intValue;  // if it's a boolean type, 0 or 1 for False or True // TODO this is a really awful way of storing a value, subclass this plz

	Variable(String name, int value){
		this.type = "int";
		this.intValue = value;
		this.name = name;
	}

	Variable(String name, boolean value){
		this.type = "bool";
		this.name = name;
		if (value){
			this.intValue = 1;
		}
		else {
			this.intValue = 0;
		}
	}

	public String getType() {
		return type;
	}

	public int getIntValue() {
		return intValue;
	}

	public String getName() {
		return name;
	}

}
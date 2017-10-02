package edu.mit.compilers.symbol_tables;

import java.util.List;
import java.util.Collections;

public abstract class Descriptor {
	protected String name;

	Descriptor (String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
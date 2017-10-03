package edu.mit.compilers.ir;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class IRType extends IRNode {

	@Override
	public List<? extends IRNode> getChildren() {
		return new ArrayList<IRNode>();
	}
	
	public static IRType getTypeFromDescriptor(TypeDescriptor descriptor) {
		return new IRType();
	}
	
	// TODO: int and bool need type descriptors
	public static IRType intType() {
		return new IRType();
	}
	
	public static IRType boolType() {
		return new IRType();
	}

}

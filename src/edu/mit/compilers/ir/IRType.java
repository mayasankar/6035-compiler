package edu.mit.compilers.ir;

import java.util.ArrayList;
import java.util.List;

public class IRType extends IRNode {

	@Override
	public List<IRNode> getChildren() {
		return new ArrayList<IRNode>();
	}
	
	// TODO: int and bool need type descriptors
	public static IRType intType() {
		return new IRType();
	}
	
	public static IRType boolType() {
		return new IRType();
	}

}

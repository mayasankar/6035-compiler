package edu.mit.compilers.ir.decl;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;

public abstract class IRMemberDecl extends IRNode {
	
	protected IRType type;
	protected String name;
	
	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList();
	}
	
	protected IRMemberDecl(IRType type, String name) {
		this.type = type;
		this.name = name;
	}
	
	public IRType getType() {
		return type;
	}

}

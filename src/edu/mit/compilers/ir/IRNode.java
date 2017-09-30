package edu.mit.compilers.ir;

import java.util.List;

public abstract class IRNode {
	protected IRNode parent;
	public abstract List<IRNode> getChildren();
}

package edu.mit.compilers.ir;

import java.util.List;

public abstract class IRNode {
	public int line = 0;
	public int column = 0;

	public String location(){
		return Integer.toString(line) + "," + Integer.toString(column);
	}

	public /*abstract*/ List<? extends IRNode> getChildren() { return null; };
}

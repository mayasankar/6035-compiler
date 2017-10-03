package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;

public class IRBreakStatement extends IRStatement {

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList();
	}

}

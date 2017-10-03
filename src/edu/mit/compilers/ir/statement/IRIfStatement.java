package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.expression.IRExpression;

public class IRIfStatement extends IRStatement {
	
	private final IRExpression condition;
	
	private final IRBlockStatement block;
	
	public IRIfStatement(IRExpression condition, IRBlockStatement block) {
		this.condition = condition;
		this.block = block;
	}
	
	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(condition, block);
	}

}

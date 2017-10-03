package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.expression.IRExpression;

public class IRForStatement extends IRStatement {
	
	private final IRStatement initializer;
	
	private final IRExpression condition;
	
	private final IRStatement stepFunction;
	
	private final IRBlockStatement block;
	
	public IRForStatement(IRStatement initializer, IRExpression condition, IRStatement stepFunction, IRBlockStatement block) {
		this.initializer = initializer;
		this.condition = condition;
		this.stepFunction = stepFunction;
		this.block = block;
	}
	
	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(initializer, condition, stepFunction, block);
	}

}

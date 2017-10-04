package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.trees.ConcreteTree;

public class IRForStatement extends IRStatement {

	private  IRStatement initializer;

	private  IRExpression condition;

	private  IRStatement stepFunction;

	private  IRBlock block;

	public IRForStatement(ConcreteTree tree) {
		statementType = IRStatement.StatementType.FOR_BLOCK;
		//TODO initialize
	}

	public IRForStatement(IRStatement initializer, IRExpression condition, IRStatement stepFunction, IRBlock block) {
		this.initializer = initializer;
		this.condition = condition;
		this.stepFunction = stepFunction;
		this.block = block;
	}

	//@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(initializer, condition, stepFunction, block);
	}

}

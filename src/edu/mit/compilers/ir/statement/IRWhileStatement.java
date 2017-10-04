package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.trees.ConcreteTree;

public class IRWhileStatement extends IRStatement {

	private IRExpression condition;

	private IRBlock block;

	public IRWhileStatement(ConcreteTree tree) {
		statementType = IRStatement.StatementType.WHILE_BLOCK;
		//TODO implement
	}

	public IRWhileStatement(IRExpression condition, IRBlock block) {
		this.condition = condition;
		this.block = block;
	}

	//@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(condition, block);
	}

}

package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.symbol_tables.VariableTable;

public class IRForStatement extends IRStatement {

	private  IRAssignStatement initializer;
	private  IRExpression condition;
	private  IRAssignStatement stepFunction;
	private  IRBlock block;
	
	public IRForStatement(IRAssignStatement initializer, IRExpression condition, 
			IRAssignStatement stepFunction, IRBlock block) {
		this.initializer = initializer;
		this.condition = condition;
		this.stepFunction = stepFunction;
		this.block = block;
		statementType = IRStatement.StatementType.FOR_BLOCK;
	}

	@Override
	String toString(int indent) {
		String whitespace = "";
		for (int i = 0; i < indent; ++i) {
			whitespace += "  ";
		}
		return whitespace + "for " + " (" + initializer + "; " + condition + "; " + stepFunction + ")"
						+ block.toString(indent + 1);
	}

	public IRBlock getBlock() { return block; }
	public IRAssignStatement getStepFunction() { return stepFunction; }
	public IRExpression getCondition() { return condition; }
	public IRAssignStatement getInitializer() { return initializer; }

	//@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(initializer, condition, stepFunction, block);
	}

}

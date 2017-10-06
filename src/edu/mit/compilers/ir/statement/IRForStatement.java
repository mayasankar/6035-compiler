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

	public IRForStatement(ConcreteTree tree, VariableTable parentScope) {
		statementType = IRStatement.StatementType.FOR_BLOCK;
		ConcreteTree child = tree.getFirstChild();
		initializer = IRAssignStatement.makeForLoopInitializer(child);
		child = child.getRightSibling().getRightSibling().getRightSibling();
		condition = IRExpression.makeIRExpression(child);
		child = child.getRightSibling();
		stepFunction = IRAssignStatement.makeForLoopStepFunction(child);
		while (!child.getName().equals("block")) {
			child = child.getRightSibling();
		}
		block = new IRBlock(child, parentScope);
	}

	@Override
	String toString(int indent) {
		String whitespace = "";
		for (int i = 0; i < indent; ++i) {
			whitespace += "  ";
		}
		return whitespace + "for " // TODO + initializer + condition + stepFunction
						+ block.toString(indent + 1);
	}



	//@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(initializer, condition, stepFunction, block);
	}

}

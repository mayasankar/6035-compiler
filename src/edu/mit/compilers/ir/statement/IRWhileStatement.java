package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.symbol_tables.VariableTable;

public class IRWhileStatement extends IRStatement {

	private IRExpression condition;
	private IRBlock block;

	public IRWhileStatement(ConcreteTree tree, VariableTable parentScope) {
		statementType = IRStatement.StatementType.WHILE_BLOCK;
		ConcreteTree child = tree.getFirstChild();
		condition = IRExpression.makeIRExpression(child);
		child = child.getRightSibling();
		block = new IRBlock(child, parentScope);
	}

	public IRWhileStatement(IRExpression condition, IRBlock block) {
		this.condition = condition;
		this.block = block;
	}

	@Override
	String toString(int indent) {
		String whitespace = "";
		for (int i = 0; i < indent; ++i) {
			whitespace += "  ";
		}
		return whitespace + "while " + condition
						+ block.toString(indent + 1);
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(condition, block);
	}

}

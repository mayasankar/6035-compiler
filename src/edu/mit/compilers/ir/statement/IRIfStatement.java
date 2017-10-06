package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.symbol_tables.VariableTable;

public class IRIfStatement extends IRStatement {

	private IRExpression ifCondition;
	private IRBlock thenBlock;
	private IRBlock elseBlock;

	public IRIfStatement(ConcreteTree tree, VariableTable parentScope) {
		statementType = IRStatement.StatementType.IF_BLOCK;
		ConcreteTree child = tree.getFirstChild();
		ifCondition = IRExpression.makeIRExpression(child);
		child = child.getRightSibling();
		thenBlock = new IRBlock(child, parentScope);
		child = child.getRightSibling();
		if (child == null) {
			elseBlock = null;
		} else {
			elseBlock = new IRBlock(child, parentScope);
		}
	}

	@Override
	String toString(int indent) {
		String whitespace = "";
		for (int i = 0; i < indent; ++i) {
			whitespace += "  ";
		}
		String answer = whitespace + "if " + ifCondition
						+ thenBlock.toString(indent + 1);
		if (elseBlock != null) {
			answer += "\n" + whitespace + "else" + elseBlock.toString(indent + 1);
		}
		return answer;
	}


	//@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(ifCondition, thenBlock, elseBlock);
	}

}

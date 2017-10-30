package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRNode.IRNodeVisitor;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.statement.IRStatement.IRStatementVisitor;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.symbol_tables.VariableTable;

public class IRIfStatement extends IRStatement {

	private IRExpression ifCondition;
	private IRBlock thenBlock;
	private IRBlock elseBlock;

	public IRIfStatement(IRExpression ifCondition, IRBlock thenBlock, IRBlock elseBlock) {
		this.ifCondition = ifCondition;
		this.thenBlock = thenBlock;
		this.elseBlock = elseBlock;
		statementType = IRStatement.StatementType.IF_BLOCK;

	}
	
	public IRIfStatement(IRExpression ifCondition, IRBlock thenBlock) {
		this.ifCondition = ifCondition;
		this.thenBlock = thenBlock;
		statementType = IRStatement.StatementType.IF_BLOCK;
	}

	public IRExpression getCondition() { return ifCondition; }
	public IRBlock getThenBlock() { return thenBlock; }
	public IRBlock getElseBlock() { return elseBlock; }

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


	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList(ifCondition, thenBlock, elseBlock);
	}
	
	@Override
	public <R> R accept(IRStatementVisitor<R> visitor) {
		return visitor.on(this);
	}

	@Override
	public <R> R accept(IRNodeVisitor<R> visitor) {
		return visitor.on(this);
	}

}

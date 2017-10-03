package edu.mit.compilers.ir.statement;

import java.util.Collections;
import java.util.List;

import edu.mit.compilers.ir.IRNode;

public class IRBlockStatement extends IRStatement {

	private final List<IRStatement> statements;
	
	public IRBlockStatement(List<IRStatement> statements) {
		this.statements = statements;
	}
	
	@Override
	public List<? extends IRNode> getChildren() {
		return Collections.unmodifiableList(statements);
	}

}

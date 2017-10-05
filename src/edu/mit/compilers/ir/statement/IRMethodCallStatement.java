package edu.mit.compilers.ir.statement;

import edu.mit.compilers.ir.expression.IRMethodCallExpression;
import edu.mit.compilers.trees.ConcreteTree;

public class IRMethodCallStatement extends IRStatement {
  private IRMethodCallExpression methodCall;

  public IRMethodCallStatement(ConcreteTree tree) {
		statementType = IRStatement.StatementType.METHOD_CALL;
    // TODO initialize
	}
}

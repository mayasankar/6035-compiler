package edu.mit.compilers.ir.statement;

import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.trees.ConcreteTree;

public class IRReturnStatement extends IRStatement {
  private IRExpression expr;

  public IRReturnStatement(ConcreteTree tree) {
    statementType = IRStatement.StatementType.RETURN_EXPR;
    ConcreteTree child = tree.getFirstChild();
    child = child.getRightSibling();
    if (child != null) {
      expr = IRExpression.makeIRExpression(child);
    } else {
      expr = null;
    }
  }
}

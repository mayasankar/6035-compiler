package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
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

  public IRReturnStatement(IRExpression expr) {
    this.statementType = IRStatement.StatementType.RETURN_EXPR;
  	this.expr = expr;
  }

  public IRExpression getReturnExpr(){
      return expr;
  }

  @Override
  public String toString() {
    return expr == null ? "void" : "return " + expr.toString();
  }

  @Override
  public List<? extends IRNode> getChildren() {
    return Arrays.asList();
  }
}

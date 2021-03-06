package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class IRReturnStatement extends IRStatement {
  private IRExpression expr; // null if void return statement


  public IRReturnStatement(IRExpression expr) {
    this.statementType = IRStatement.StatementType.RETURN_EXPR;
  	this.expr = expr;
  }

  public IRExpression getReturnExpr(){
      if (expr == null) {
          throw new RuntimeException("Trying to access expression of void return statement. Use isVoid() before calling getReturnExpr() to fix.");
      }
      return expr;
  }

  public boolean isVoid() { return expr == null; }

  public TypeDescriptor getReturnType() {
      return expr == null ? TypeDescriptor.VOID : expr.getType();
  }

  @Override
  public String toString() {
    return expr == null ? "void" : "return " + expr.toString();
  }

  @Override
  public List<? extends IRNode> getChildren() {
    return Arrays.asList();
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

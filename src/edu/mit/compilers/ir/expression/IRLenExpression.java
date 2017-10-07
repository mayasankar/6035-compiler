package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;

public class IRLenExpression extends IRExpression {
  private Token id; // what you are taking the length of

  public IRLenExpression(Token id) {
    expressionType = IRExpression.ExpressionType.LEN;
    this.id = id;
  }

  public String getArgument() {
      return id.getText();
  }

  @Override
  public IRType.Type getType() {
    return null;
  }

  @Override
  public List<? extends IRNode> getChildren() {
    return Arrays.asList();
  }
}

package edu.mit.compilers.ir.expression;

import antlr.Token;

import edu.mit.compilers.ir.IRType;

public class IRLenExpression extends IRExpression {
  private Token id; // what you are taking the length of

  public IRLenExpression(Token id) {
    this.id = id;
  }

  public String getArgument() {
      return id.getText();
  }

  @Override
  public IRType.Type getType() {
    return null;
  }
}

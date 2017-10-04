package edu.mit.compilers.trees;

import antlr.Token;

public class ConcreteTreeNode extends ConcreteTree {
  private Token tk;

  ConcreteTreeNode(Token t) {
    super("TOKEN " + t.getText());
    tk = t;
  }

  ConcreteTreeNode(Token t, ConcreteTree p) {
    super("TOKEN " + t.getText(), p);
    tk = t;
  }

  @Override
  public boolean isNode() {
    return true;
  }

  @Override
  public Token getToken() { return tk; }

}

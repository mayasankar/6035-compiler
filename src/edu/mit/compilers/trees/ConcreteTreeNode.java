package edu.mit.compilers.trees;

import antlr.Token;

import edu.mit.compilers.grammar.DecafParserTokenTypes;

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
  public boolean isOperator() {
    int type = tk.getType();
    return type == DecafParserTokenTypes.OP_EQ ||
           type == DecafParserTokenTypes.OP_REL ||
           type == DecafParserTokenTypes.OP_AND ||
           type == DecafParserTokenTypes.OP_OR ||
           type == DecafParserTokenTypes.OP_INC ||
           type == DecafParserTokenTypes.OP_DEC ||
           type == DecafParserTokenTypes.OP_ASSIGN_EQ ||
           type == DecafParserTokenTypes.OP_COMPOUND_ASSIGN ||
           type == DecafParserTokenTypes.OP_NEG ||
           type == DecafParserTokenTypes.OP_ADD ||
           type == DecafParserTokenTypes.OP_MUL ||
           type == DecafParserTokenTypes.OP_NOT ||
           type == DecafParserTokenTypes.OP_TERN_1 ||
           type == DecafParserTokenTypes.OP_TERN_2;
  }

  @Override
  public Token getToken() { return tk; }

  @Override
  public void initializeLineNumbers() {
    lineNumber = tk.getLine();
    columnNumber = tk.getColumn();
  }

}

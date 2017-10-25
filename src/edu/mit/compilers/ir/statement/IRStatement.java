package edu.mit.compilers.ir.statement;

import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.symbol_tables.VariableTable;

public abstract class IRStatement extends IRNode {
  public enum StatementType {
    UNSPECIFIED,
    ASSIGN_EXPR,
    METHOD_CALL,
    IF_BLOCK,
    FOR_BLOCK,
    WHILE_BLOCK,
    RETURN_EXPR,
    BREAK,
    CONTINUE
  }

  protected StatementType statementType = StatementType.UNSPECIFIED;

  @Override
  public String toString() {
      return toString(0);
  }

  String toString(int indent) {
    String answer = "";
    for (int i = 0; i < indent; ++i) {
      answer += "  ";
    }
    return answer + statementType.name() + " " + this.toString();
  }

  public StatementType getStatementType() { return statementType; }
}

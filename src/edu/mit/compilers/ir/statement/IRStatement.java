package edu.mit.compilers.ir.statement;

import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.symbol_tables.VariableTable;

public abstract class IRStatement extends IRNode {
  protected enum StatementType {
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

  String toString(int indent) {
    String answer = "";
    for (int i = 0; i < indent; ++i) {
      answer += "  ";
    }
    return answer + statementType.name() + " " + this.toString();
  }

  public static IRStatement makeIRStatement(ConcreteTree tree, VariableTable parentScope) {
    IRStatement toReturn = null;
    ConcreteTree child = tree.getFirstChild();
    if (child.isNode()) {
      int tokentype = child.getToken().getType();
      if (tokentype == DecafParserTokenTypes.TK_return) {
        toReturn = new IRReturnStatement(tree);
      } else if (tokentype == DecafParserTokenTypes.TK_break) {
        toReturn = new IRLoopStatement(StatementType.BREAK);
      } else if (tokentype == DecafParserTokenTypes.TK_continue) {
        toReturn = new IRLoopStatement(StatementType.CONTINUE);
      }
    } else {
      String name = child.getName();
      if (name.equals("assign_expr")) {
        toReturn = new IRAssignStatement(child);
      } else if (name.equals("method_call")) {
        toReturn = new IRMethodCallStatement(child);
      } else if (name.equals("if_block")) {
        toReturn = new IRIfStatement(child, parentScope);
      } else if (name.equals("for_block")) {
        toReturn = new IRForStatement(child, parentScope);
      } else if (name.equals("while_block")) {
        toReturn = new IRWhileStatement(child, parentScope);
      }
    }
    if (toReturn != null) {
      toReturn.setLineNumbers(tree);
      return toReturn;
    }
    return null;
  }
}

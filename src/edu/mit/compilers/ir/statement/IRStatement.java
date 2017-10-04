package edu.mit.compilers.ir.statement;

import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.trees.ConcreteTree;

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

  public static IRStatement makeIRStatement(ConcreteTree tree) {
    ConcreteTree child = tree.getFirstChild();
    if (child.isNode()) {
      int tokentype = child.getToken().getType();
      if (tokentype == DecafParserTokenTypes.TK_return) {
        return new IRReturnStatement(tree);
      } else if (tokentype == DecafParserTokenTypes.TK_break) {
        return new IRLoopStatement(StatementType.BREAK);
      } else if (tokentype == DecafParserTokenTypes.TK_continue) {
        return new IRLoopStatement(StatementType.CONTINUE);
      }
    } else {
      String name = child.getName();
      if (name.equals("assign_expr")) {
        return new IRAssignStatement();
      } else if (name.equals("method_call")) {
        return new IRMethodCallStatement();
      } else if (name.equals("if_block")) {
        return new IRIfStatement(child);
      } else if (name.equals("for_block")) {
        return new IRForStatement(child);
      } else if (name.equals("while_block")) {
        return new IRWhileStatement(child);
      }
    }
    return null;
  }
}

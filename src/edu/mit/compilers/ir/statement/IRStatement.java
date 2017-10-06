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
      System.out.println("Making IRStatement");
    ConcreteTree child = tree.getFirstChild();
    System.out.println("Step?");
    if (child.isNode()) {
        System.out.println("Test: should fail after this");
      int tokentype = child.getToken().getType();
      System.out.println("Nope, didn't");
      if (tokentype == DecafParserTokenTypes.TK_return) {
        return new IRReturnStatement(tree);
      } else if (tokentype == DecafParserTokenTypes.TK_break) {
        return new IRLoopStatement(StatementType.BREAK);
      } else if (tokentype == DecafParserTokenTypes.TK_continue) {
        return new IRLoopStatement(StatementType.CONTINUE);
      }
    } else {
      String name = child.getName();
      System.out.println("Got name: " + name);
      if (name.equals("assign_expr")) {
        return new IRAssignStatement(child);
      } else if (name.equals("method_call")) {
        return new IRMethodCallStatement(child);
      } else if (name.equals("if_block")) {
        return new IRIfStatement(child, parentScope);
      } else if (name.equals("for_block")) {
        return new IRForStatement(child, parentScope);
      } else if (name.equals("while_block")) {
        return new IRWhileStatement(child, parentScope);
      }
    }
    return null;
  }
}

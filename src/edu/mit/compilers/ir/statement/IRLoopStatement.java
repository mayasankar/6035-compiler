package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;

public class IRLoopStatement extends IRStatement {
  private IRStatement loop;

  public IRLoopStatement(IRStatement.StatementType st) {
    statementType = st;
  }

  public static IRLoopStatement breakStatement = new IRLoopStatement(IRStatement.StatementType.BREAK);

  public static IRLoopStatement continueStatement = new IRLoopStatement(IRStatement.StatementType.CONTINUE);

  public void setLoop(IRStatement loop) {
    this.loop = loop;
  }

  @Override
  public String toString() {
      if (statementType == IRStatement.StatementType.BREAK) {
          return "break";
      }
      if (statementType == IRStatement.StatementType.CONTINUE) {
          return "continue";
      }
      return "";
  }

  @Override
  public List<? extends IRNode> getChildren() {
    return Arrays.asList();
  }
}

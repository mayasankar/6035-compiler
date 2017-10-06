package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.IRNode;

public class IRLoopStatement extends IRStatement {
  public IRLoopStatement(IRStatement.StatementType st) {
    statementType = st;
  }

  @Override
  public String toString() {
    return "";
  }

  @Override
  public List<? extends IRNode> getChildren() {
    return Arrays.asList();
  }
}

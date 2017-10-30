package edu.mit.compilers.ir.statement;

import edu.mit.compilers.ir.IRNode;

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
  
	public interface IRStatementVisitor<R>{
		public R on(IRAssignStatement ir);
		public R on(IRForStatement ir);
		public R on(IRIfStatement ir);
		public R on(IRLoopStatement ir);
		public R on(IRMethodCallStatement ir);
		public R on(IRReturnStatement ir);
		public R on(IRWhileStatement ir);
	}
	
	public abstract <R> R accept(IRStatementVisitor<R> visitor);

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

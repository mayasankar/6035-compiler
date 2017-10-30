package edu.mit.compilers.ir.expression;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.IRNode.IRNodeVisitor;
import edu.mit.compilers.ir.expression.IRExpression.IRExpressionVisitor;

public class IRLenExpression extends IRExpression {
  private Token id; // what you are taking the length of

  public IRLenExpression(Token id) {
    expressionType = IRExpression.ExpressionType.LEN;
    this.id = id;
  }

  public String getArgument() {
      return id.getText();
  }

  @Override
  public IRType.Type getType() {
    return IRType.Type.INT;
  }
  
	@Override
	public int getDepth() {
		return 1;
	}

    @Override
    public List<IRExpression> getChildren() {
        // TODO Auto-generated method stub
        return Arrays.asList();
    }
    
	@Override
	public <R> R accept(IRExpressionVisitor<R> visitor) {
		return visitor.on(this);
	}

	@Override
	public <R> R accept(IRNodeVisitor<R> visitor) {
		return visitor.on(this);
	}
}

package edu.mit.compilers.ir.statement;

import java.util.Arrays;
import java.util.List;

import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRMethodCallExpression;
import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.trees.ConcreteTree;

public class IRMethodCallStatement extends IRStatement {
    private IRMethodCallExpression methodCall;

    public IRMethodCallStatement(IRExpression expr) {
      	if(!(expr instanceof IRMethodCallExpression)) {
      		throw new RuntimeException("To make a method call statement you must supply a method call expression");
      	}
      	methodCall = (IRMethodCallExpression) expr;
        statementType = IRStatement.StatementType.METHOD_CALL;
    }

    public IRMethodCallStatement(ConcreteTree tree) {
		statementType = IRStatement.StatementType.METHOD_CALL;
        methodCall = new IRMethodCallExpression(tree);
    }

    public IRMethodCallExpression getMethodCall() { return methodCall; }

    @Override
    public String toString() {
        return methodCall.toString();
    }

    @Override
    public List<? extends IRNode> getChildren() {
        return Arrays.asList(methodCall);
    }
}

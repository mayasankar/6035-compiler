package edu.mit.compilers.ir;

import java.util.List;

import antlr.Token;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.IRLiteral;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.MethodTable;
import edu.mit.compilers.symbol_tables.VariableTable;
import edu.mit.compilers.trees.ConcreteTree;

public abstract class IRNode {
	public int line = -1;
	public int column = -1;
	protected VariableTable variableScope;
	protected MethodTable methodTable;
	
	public interface IRNodeVisitor<R>{
		public R on(IRProgram ir);
		
		public R on(IRFieldDecl ir);
		public R on(IRLocalDecl ir);
		public R on(IRParameterDecl ir);
		public R on(IRMethodDecl ir);

		public R on(IRUnaryOpExpression ir);
		public R on(IRBinaryOpExpression ir);
		public R on(IRTernaryOpExpression ir);
		public R on(IRLenExpression ir);
		public R on(IRVariableExpression ir);
		public R on(IRMethodCallExpression ir);
		public <T> R on(IRLiteral<T> ir);
		
		public R on(IRAssignStatement ir);
		public R on(IRBlock ir);
		public R on(IRForStatement ir);
		public R on(IRIfStatement ir);
		public R on(IRLoopStatement ir);
		public R on(IRMethodCallStatement ir);
		public R on(IRReturnStatement ir);
		public R on(IRWhileStatement ir);
	}
	
	public abstract <R> R accept(IRNodeVisitor<R> visitor);

	public String location(){
		return Integer.toString(line) + "," + Integer.toString(column);
	}

	public void setLineNumbers(int line, int column) {
		this.line = line;
		this.column = column;
	}

	public void setLineNumbers(ConcreteTree tree) {
		line = tree.getLine();
		column = tree.getColumn();
	}

	public void setLineNumbers(Token tk) {
		line = tk.getLine();
		column = tk.getColumn();
	}

	public void setLineNumbers(IRNode node) {
		this.line = node.line;
		this.column = node.column;
	}

	// returns true if this comes after other, via line + col numbers.
	// returns false if other = this.
	public boolean comesAfter(IRNode other) {
		if (this.line < 0 || other.line < 0) {
			throw new RuntimeException("Comparing IRNode with uninitialized line number");
		} else if (this.line != other.line) {
			return this.line > other.line;
		} else {
			if (this.column < 0 || other.column < 0) {
				throw new RuntimeException("Comparing IRNode with uninitialized column number");
			}
			return this.column > other.column;
		}
	}

	public abstract List<? extends IRNode> getChildren();

	public void setTables(VariableTable varTable, MethodTable methodTable) {
		this.variableScope = varTable;
		this.methodTable = methodTable;
	}

	public MethodTable getMethodTable() {
		return methodTable;
	}

	public VariableTable getVariableTable() {
		return variableScope;
	}
}

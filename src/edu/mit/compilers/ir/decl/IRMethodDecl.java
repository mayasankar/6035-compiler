package edu.mit.compilers.ir.decl;

import java.util.List;
import java.util.Arrays;

import antlr.Token;

import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.IRNode.IRNodeVisitor;
import edu.mit.compilers.ir.statement.IRBlock;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.symbol_tables.Named;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.symbol_tables.VariableTable;
import edu.mit.compilers.symbol_tables.VariableDescriptor;

public class IRMethodDecl extends IRNode implements Named {
  TypeDescriptor returnType = TypeDescriptor.UNSPECIFIED;
  protected Token id;
  // ArrayList<IRParameterDecl> parameters = new ArrayList<IRParameterDecl>();
  IRBlock code; // null if import declaration
  VariableTable parameters; // null if import declaration

  // used only by IRImportDecl
  protected IRMethodDecl(Token id) {
    this.id = id;
    returnType = TypeDescriptor.INT;
    code = null;
    parameters = null;
    setLineNumbers(id);
  }
  
  public IRMethodDecl(TypeDescriptor returnType, Token id, VariableTable parameters, IRBlock code) {
	  this.returnType = returnType;
	  this.id = id;
	  this.code = code;
	  this.parameters = parameters;
  }

  public String getName() { return id.getText(); }
  public TypeDescriptor getReturnType() { return returnType; }
  public IRBlock getCode() {
      if (isImport()) {
          throw new RuntimeException("Calling getCode on import declaration.");
      }
      return code;
  }
  public void setCode(IRBlock code) {
      if (isImport()) {
          throw new RuntimeException("Calling getCode on import declaration.");
      }
      this.code = code;
  }
  public VariableTable getParameters() {
      if (isImport()) {
          throw new RuntimeException("Calling getParameters on import declaration.");
      }
      return parameters;
  }

  public boolean isImport() { return false; } // overriden in import subclass

  @Override
  public List<? extends IRNode> getChildren() {
    return Arrays.asList(code);
  }

  @Override
  public String toString() {
    String answer = "Method " + id.getText() + ". ";
    answer += parameters.toString("Arguments");
    answer += code.toString(1);
    return answer;
  }
  
	@Override
	public <R> R accept(IRNodeVisitor<R> visitor) {
		return visitor.on(this);
	}
}

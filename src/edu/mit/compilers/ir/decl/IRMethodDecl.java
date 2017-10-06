package edu.mit.compilers.ir.decl;

import java.util.List;
import java.util.Arrays;

import antlr.Token;

import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.statement.IRBlock;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.symbol_tables.VariableTable;

public class IRMethodDecl extends IRNode {
  IRType.Type returnType = IRType.Type.UNSPECIFIED;
  Token id;
  // ArrayList<IRParameterDecl> parameters = new ArrayList<IRParameterDecl>();
  IRBlock code;
  VariableTable parameters;

  public IRMethodDecl(ConcreteTree tree, VariableTable parentScope) {
    setLineNumbers(tree);

    parameters = new VariableTable(parentScope);

    ConcreteTree child = tree.getFirstChild();
    switch (child.getToken().getType()) {
      case DecafParserTokenTypes.TK_int: {
        returnType = IRType.Type.INT; break;
      }
      case DecafParserTokenTypes.TK_bool: {
        returnType = IRType.Type.BOOL; break;
      }
      case DecafParserTokenTypes.TK_void: {
        returnType = IRType.Type.VOID; break;
      }
    }
    child = child.getRightSibling();
    id = child.getToken();
    child = child.getRightSibling();
    while(child.isNode()) {
      IRType.Type parameterType = IRType.getType(child.getToken());
      child = child.getRightSibling();
      Token parameterId = child.getToken();
      parameters.add(new IRParameterDecl(parameterType, parameterId));
      child = child.getRightSibling();
    }
    code = new IRBlock(child, parameters);
  }

  public String getName() { return id.getText(); }
  public IRType.Type getReturnType() { return returnType; }
  public IRBlock getCode() { return code; }
  public VariableTable getParameters() { return parameters; }

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
}

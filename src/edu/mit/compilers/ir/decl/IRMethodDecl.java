package edu.mit.compilers.ir.decl;

import java.util.ArrayList;

import antlr.Token;

import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.statement.IRBlock;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.symbol_tables.VariableTable;

public class IRMethodDecl {
  IRType.Type returnType = IRType.Type.UNSPECIFIED;
  Token id;
  ArrayList<IRParameterDecl> parameters = new ArrayList<IRParameterDecl>();
  IRBlock code;
  VariableTable parameterTable = new VariableTable();  // TODO make this an actual thing

  public IRMethodDecl(ConcreteTree tree) {
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
    code = new IRBlock(child, parameterTable);
  }

  public String getName() {
    return id.getText();
  }

  @Override
  public String toString() {
    String answer = "Method " + id.getText() + ". Arguments: ";
    if (parameters.size() == 0) {
      answer += "none";
    } else {
      for (IRParameterDecl parameter : parameters) {
        answer += parameter.toString() + ", ";
      }
    }
    answer += code.toString(1);
    return answer;
  }
}
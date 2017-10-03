package edu.mit.compilers.trees;

import antlr.Token;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.ir.expression.IRExpression;

// This class has a lot of the functions necessary to simplify the concrete tree
// into an abstract tree.

public abstract class ASTCreator {

  public static void simplifyTree(ConcreteTree tree) {
    // delete tokens only necessary for parsing
    tree.deleteNodes(DecafParserTokenTypes.EOF);
    tree.deleteNodes(DecafParserTokenTypes.COMMA);
    tree.deleteNodes(DecafParserTokenTypes.LPAREN);
    tree.deleteNodes(DecafParserTokenTypes.RPAREN);
    tree.deleteNodes(DecafParserTokenTypes.LCURLY);
    tree.deleteNodes(DecafParserTokenTypes.RCURLY);
    tree.deleteNodes(DecafParserTokenTypes.SEMICOLON);
    // contract along unnecessary edges
    tree.compressNodes("expr");
    for (int i = 0; i <= 8; ++i) {
      tree.compressNodes("expr_" + i);
    }
    tree.compressNodes("op_pm");
    tree.compressNodes("bool_literal");
  }
}

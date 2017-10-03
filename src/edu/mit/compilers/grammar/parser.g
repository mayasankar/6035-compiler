header {
package edu.mit.compilers.grammar;

import edu.mit.compilers.trees.ConcreteTree;
}

options
{
  mangleLiteralPrefix = "TK_";
  language = "Java";
}

class DecafParser extends Parser;
options
{
  importVocab = DecafScanner;
  k = 3;
  buildAST = true;
}

// Java glue code that makes error reporting easier.
// You can insert arbitrary Java code into your parser/lexer this way.
{
  // Do our own reporting of errors so the parser can return a non-zero status
  // if any errors are detected.
  /** Reports if any errors were reported during parse. */
  private boolean error;

  @Override
  public void reportError (RecognitionException ex) {
    // Print the error via some kind of error reporting mechanism.
    error = true;
  }
  @Override
  public void reportError (String s) {
    // Print the error via some kind of error reporting mechanism.
    error = true;
  }
  public boolean getError () {
    return error;
  }

  private ConcreteTree parseTree = ConcreteTree.root();

  public ConcreteTree getParseTree() { return parseTree; }

  @Override
  public void match(int t) throws MismatchedTokenException, TokenStreamException {
    parseTree.addNode(LT(1));
    super.match(t);
  }

  // Selectively turns on debug mode.

  /** Whether to display debug information. */
  private boolean trace = false;

  public void setTrace(boolean shouldTrace) {
    trace = shouldTrace;
  }
  @Override
  public void traceIn(String rname) throws TokenStreamException {
    parseTree = parseTree.addChild(rname);
    if (trace) {
      super.traceIn(rname);
    }
  }
  @Override
  public void traceOut(String rname) throws TokenStreamException {
    parseTree = parseTree.getParent();
    if (trace) {
      super.traceOut(rname);
    }
  }
}

program : (import_decl)* (field_decl)* (method_decl)* EOF;
import_decl : TK_import ID SEMICOLON;
type : TK_int | TK_bool;
id_decl : ID | ID LBRACKET INT RBRACKET;
field_decl : type id_decl (COMMA id_decl)* SEMICOLON;

method_decl : (type | TK_void) ID LPAREN (type ID (COMMA type ID)*)? RPAREN block;
block : LCURLY (field_decl)* (statement)* RCURLY;

statement : assign_expr
          | method_call SEMICOLON
          | if_block
          | for_block
          | while_block
          | TK_return (expr)? SEMICOLON
          | TK_break SEMICOLON
          | TK_continue SEMICOLON;
assign_expr : location (((OP_ASSIGN_EQ | OP_COMPOUND_ASSIGN) expr) | OP_INC | OP_DEC) SEMICOLON;
if_block : TK_if LPAREN expr RPAREN block (TK_else block)?;
for_block : TK_for LPAREN
                ID OP_ASSIGN_EQ expr SEMICOLON
                expr SEMICOLON
                location (OP_COMPOUND_ASSIGN expr | OP_INC | OP_DEC) RPAREN block;
while_block : TK_while LPAREN expr RPAREN block;

expr : expr_8;

method_call : ID LPAREN ((expr|STRING) (COMMA (expr|STRING))*)? RPAREN;
location : ID | ID LBRACKET expr RBRACKET;
literal : INT | CHAR | bool_literal;
bool_literal : TK_true | TK_false;

// expr by order of operations
protected op_pm : OP_NEG | OP_ADD;
expr_0 : location | method_call | literal
       | TK_len LPAREN ID RPAREN
       | LPAREN expr RPAREN;
expr_1 : (OP_NOT | OP_NEG)* expr_0;
expr_2 : expr_1 (OP_MUL expr_1)*;
expr_3 : expr_2 (op_pm expr_2)*;
expr_4 : expr_3 (OP_REL expr_3)*;
expr_5 : expr_4 (OP_EQ expr_4)*;
expr_6 : expr_5 (OP_AND expr_5)*;
expr_7 : expr_6 (OP_OR expr_6)*;
expr_8 : expr_7 (OP_TERN_1 expr_8 OP_TERN_2 expr_8)*;

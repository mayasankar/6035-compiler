header {
package edu.mit.compilers.grammar;
}

options
{
  mangleLiteralPrefix = "TK_";
  language = "Java";
}

{@SuppressWarnings("unchecked")}
class DecafScanner extends Lexer;
options
{
  k = 2;
}

tokens
{
  "bool";
  "break";
  "class";
  "continue";
  "else";
  "false";
  "for";
  "if";
  "import";
  "int";
  "len";
  "return";
  "true";
  "void";
  "while";
}

// Selectively turns on debug tracing mode.
// You can insert arbitrary Java code into your parser/lexer this way.
{
  /** Whether to display debug information. */
  private boolean trace = false;

  public void setTrace(boolean shouldTrace) {
    trace = shouldTrace;
  }
  @Override
  public void traceIn(String rname) throws CharStreamException {
    if (trace) {
      super.traceIn(rname);
    }
  }
  @Override
  public void traceOut(String rname) throws CharStreamException {
    if (trace) {
      super.traceOut(rname);
    }
  }

}

LCURLY options { paraphrase = "{"; } : "{";
RCURLY options { paraphrase = "}"; } : "}";
LBRACKET options { paraphrase = "["; } : '[';
RBRACKET options { paraphrase = "]"; } : ']';
LPAREN options { paraphrase = "("; } : '(';
RPAREN options { paraphrase = ")"; } : ')';
SEMICOLON options { paraphrase = ";"; } : ';';
COMMA options { paraphrase = ","; } : ',';

protected
ID_START_CHAR : ('a'..'z' | 'A'..'Z' | '_');
protected
ID_ANY_CHAR : ID_START_CHAR | DIGIT;
ID options { paraphrase = "an identifier"; } :
  ID_START_CHAR (ID_ANY_CHAR)*;


// ================
// OPERATOR RULES
// ================

// TODO make sure whitespace after operator is a thing
// Change print value of whitespace to '' so you can append it to end of operator

OP_EQ : "==" | "!=";
OP_REL : ('<'|'>') ('='|);
OP_AND : "&&";
OP_OR : "||";
OP_INC : "++";
OP_DEC : "--";
OP_ASSIGN_EQ : '=';
OP_COMPOUND_ASSIGN : "+=" | "-=";
OP_NEG : '-';
OP_ADD : '+';
OP_MUL : '*' | '/' | '%';
OP_NOT : '!';
OP_TERN_1 : '?';
OP_TERN_2 : ':';

// OLD CODE
// OPERATOR : (('>'|'<'|'='|'!') ('='|))
//          | ('+' ('='|'+'|)) | ('-' ('='|'-'|))
//          | "&&"|"||"
//          | '*'|'/'
//          | '?'|':';
// OPERATOR : ">="|"<="|"=="|"!="|"&&"|"||"|"++"|"--"|"+="|"-="|'-'|'*'|'/'|'+'|'>'|'<'|'!'|'?'|':';

// ================
// WHITESPACE AND COMMENTS
// ================

WS_ : (' ' | '\t' | NEWLINE ) { _ttype = Token.SKIP; };
SL_COMMENT : "//" (~'\n')* '\n' { _ttype = Token.SKIP; newline(); };
// TODO make this a nested inline comment
IL_COMMENT : "/*" (~('\n'|'*') | NEWLINE | '*' ~'/')* "*/" { _ttype = Token.SKIP; };

// ================
// INT RULES
// ================

protected
DIGIT : ('0'..'9');
protected
HEX_DIGIT : DIGIT | ('a'..'f') | ('A'..'F');

INT : (DIGIT)+ | "0x" (HEX_DIGIT)+;

// ================
// STRING RULES
// ================

protected
// done like this instead of with ~ because why?
STRING_CHARACTER : ESC|' '|'!'|('#'..'&')|('('..'[')|(']'..'~');
protected
NEWLINE : '\n' { newline(); };
protected
ESC :  '\\' ('\''|'"'|'\\'|'n'|'t');

CHAR : '\'' (STRING_CHARACTER
exception
catch [NoViableAltForCharException ex] {
  if (ex.foundChar == '\n'){
    newline();
    setColumn(0);
  }
  throw ex;
}) '\'';
STRING : '"' (STRING_CHARACTER)* '"';

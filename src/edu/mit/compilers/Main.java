package edu.mit.compilers;

import java.io.*;
import java.util.Map;

import antlr.CharStreamException;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStreamException;
import antlr.collections.AST;

import edu.mit.compilers.grammar.*;
import edu.mit.compilers.tools.CLI;
import edu.mit.compilers.tools.CLI.Action;
import edu.mit.compilers.trees.ASTCreator;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.trees.SemanticCheckerVisitor;
import edu.mit.compilers.cfg.*;

class Main {
  public static void main(String[] args) {
    try {
      CLI.parse(args, new String[0]);
      InputStream inputStream = args.length == 0 ?
          System.in : new java.io.FileInputStream(CLI.infile);
      PrintStream outputStream = CLI.outfile == null ? System.out
          : new java.io.PrintStream(new java.io.FileOutputStream(CLI.outfile));

      if (CLI.target == Action.SCAN) {
          scan(inputStream, outputStream);
      } else if (CLI.target == Action.PARSE) {
          parse(inputStream, outputStream);
      } else if (CLI.target == Action.INTER || CLI.target == Action.ASSEMBLY ||
                 CLI.target == Action.DEFAULT) {

        DecafParser parser = parse(inputStream, outputStream);
        ConcreteTree tree = parser.getParseTree();
        if (CLI.debug) {
          ASTCreator.simplifyTree(tree);
          tree.print();
        }
        IRProgram ir = ASTCreator.getIRNew(tree);
        if (CLI.debug) {
            System.out.println(ir);
        }

        SemanticCheckerVisitor checker = new SemanticCheckerVisitor();
        if (ir.accept(checker)) {
            System.exit(1);
        }
        checker.renameVariables();
        if (CLI.debug) {
            System.out.println("Renamed variables in IR");
            System.out.println(ir);
        }

        if (CLI.target == Action.ASSEMBLY){
            CFGCreator creator = new CFGCreator();
            Map<String, CFGBlock> graphs = creator.destruct(ir);
            Assembler assembler = new Assembler();
            assembler.makeCode(graphs, outputStream, ir.getMethodTable(), ir.getVariableTable());
        }
      }
    } catch(Exception e) {
        System.err.println(CLI.infile+" "+e);
        e.printStackTrace();
    }
  }

    private static void scan(InputStream inputStream, PrintStream outputStream) throws CharStreamException {
        DecafScanner scanner = new DecafScanner(new DataInputStream(inputStream));
        scanner.setTrace(CLI.debug);
        Token token;
        boolean done = false;
        while (!done) {
            try {
                for (token = scanner.nextToken();
                     token.getType() != DecafParserTokenTypes.EOF;
                     token = scanner.nextToken()) {
                    String type = "";
                    String text = token.getText();
                    switch (token.getType()) {
                    case DecafScannerTokenTypes.CHAR:
                      type = " CHARLITERAL";
                      break;
                    case DecafScannerTokenTypes.ID:
                      type = " IDENTIFIER";
                      break;
                    case DecafScannerTokenTypes.INT:
                      type = " INTLITERAL";
                      break;
                    case DecafScannerTokenTypes.STRING:
                      type = " STRINGLITERAL";
                      break;
                    case DecafScannerTokenTypes.TK_true:
                    case DecafScannerTokenTypes.TK_false:
                      type = " BOOLEANLITERAL";
                      break;
                  }
                  outputStream.println(token.getLine() + type + " " + text);
                }
                done = true;
            } catch(Exception e) {
                System.err.println(CLI.infile + " " + e);
                scanner.consume();
            }
        }
    }

    private static DecafParser parse(InputStream inputStream, PrintStream outputStream) throws RecognitionException, TokenStreamException {
        DecafScanner scanner = new DecafScanner(new DataInputStream(inputStream));
        DecafParser parser = new DecafParser(scanner);
        parser.setTrace(CLI.debug);
        parser.program();
        if (parser.getError()) {
            System.exit(1);
        }

        return parser;
    }
}

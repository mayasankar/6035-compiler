package edu.mit.compilers;

import java.io.*;
import antlr.Token;
import antlr.collections.AST;
import edu.mit.compilers.grammar.*;
import edu.mit.compilers.tools.CLI;
import edu.mit.compilers.tools.CLI.Action;
import edu.mit.compilers.trees.ASTCreator;
import edu.mit.compilers.trees.ConcreteTree;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.trees.SemanticChecker;
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
        DecafScanner scanner =
            new DecafScanner(new DataInputStream(inputStream));
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
               // TODO: add strings for the other types here...
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
            // print the error:
            System.err.println(CLI.infile + " " + e);
            scanner.consume();
          }
        }
      } else if (CLI.target == Action.PARSE) {
        DecafScanner scanner =
            new DecafScanner(new DataInputStream(inputStream));
        DecafParser parser = new DecafParser(scanner);
        parser.setTrace(CLI.debug);
        parser.program();
        if (parser.getError()) {
          System.exit(1);
        }
      } else if (CLI.target == Action.INTER ||
                 CLI.target == Action.DEFAULT) { // TODO do something if CLI.debug
        DecafScanner scanner =
            new DecafScanner(new DataInputStream(inputStream));
        DecafParser parser = new DecafParser(scanner);
        parser.program();
        if (parser.getError()) {
          System.exit(1);
        }
        ConcreteTree tree = parser.getParseTree();
        if (CLI.debug) {
          ASTCreator.simplifyTree(tree);
          tree.print();
        }
        IRProgram ir = ASTCreator.getIRNew(tree);
        if (CLI.debug) {
            System.out.println(ir);
        }

        SemanticChecker checker = new SemanticChecker();
        if (checker.checkProgram(ir)) {
            System.exit(1);
        }

        CFGCreator creator = new CFGCreator();
        CFG graph = creator.destruct(ir);
        System.out.println(graph.toString());
      }
    } catch(Exception e) {
      // print the error:
      System.err.println(CLI.infile+" "+e);
      e.printStackTrace();
    }
  }
}

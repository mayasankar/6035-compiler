package edu.mit.compilers.trees;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.operator.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;

public class SemanticChecker {

    private EnvStack env = new EnvStack();

    public void checkProgram(IRProgram tree){
        notifyError("DEBUG: Checking program for semantic errors.", tree);
        checkProgramTable(tree.getProgramTable());
    }

    private void notifyError(String error, Object problematicObject){
      // TODO make this better; have location and whatnot
      System.out.println(error);
    }

    private void checkProgramTable(ProgramTable table){
        env.push(table.getMethodTable());
        env.push(table.getFields());
        checkImports(table.getImports());
        checkVariableTable(table.getFields());
        checkMethodTable(table.getMethodTable());
    }

    private void checkImports(List<Token> imports){
      // check that they're all distinct
      HashSet<Token> importsSet = new HashSet<>();
      for (Token imp : imports){
        if (importsSet.contains(imp)){
          notifyError("Attempted to import an identifier previously imported.", imp);
        }
        importsSet.add(imp);
      }
      // TODO maybe we also need to check these against global variables?
    }

    private void checkMethodTable(MethodTable table){
      // TODO
    }

    private void checkVariableTable(VariableTable table){
      // TODO
    }

    private void checkVariable(IRMemberDecl variable){
      // TODO
    }

    private void checkMethod(IRMethodDecl method){
      // TODO
    }

    private void checkBlock(IRBlock block){
      for (IRStatement s : block.getStatements()){
        checkStatement(s);
      }
    }

    private void checkStatement(IRStatement statement){
      // TODO
    }

    private void checkIRVariableExpression(IRVariableExpression expr){
      // TODO
    }

    // TODO probably lots more of these?

}

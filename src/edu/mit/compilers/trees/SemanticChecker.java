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

// write semantic checks 1,2,4,7,10,11,12,13,14,15,16,17,18,19,20,21
// test  semantic checks 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21

public class SemanticChecker {

    private EnvStack env = new EnvStack();

    public void checkProgram(IRProgram tree){
        notifyError("DEBUG: Checking program for semantic errors.", tree);
        env.push(tree.methods);
        env.push(tree.fields);
        env.push(IRType.Type.VOID);
        checkImports(tree.imports);
        checkVariableTable(tree.fields);
        checkMethodTable(tree.methods);
        checkHasMain(tree);
        env.popMethods();
        env.popVariables();
        env.popReturnType();
    }

    private void notifyError(String error, IRNode problematicNode){
        System.out.println("ERROR " + problematicNode.location() + ": " + error);
    }

    private void checkHasMain(IRProgram program){
        // 3
        IRMethodDecl mainMethod = program.methods.get("main");
        if (mainMethod == null) {
            notifyError("Program has no main method.", program);
        }
        if (mainMethod.getReturnType() != IRType.Type.VOID){
            notifyError("Main method return type is not void.", mainMethod);
        }

        if (! mainMethod.getParameters().isEmpty()){
            notifyError("Main method requires input parameters.", mainMethod);
        }
    }

    private void checkImports(List<IRImportDecl> imports){
        // first part of 1
        // check that they're all distinct
        HashSet<IRImportDecl> importsSet = new HashSet<>();
        for (IRImportDecl imp : imports){
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

    private void checkReturnStatement(IRReturnStatement statement){
        // 8, 9
        IRType.Type desiredReturnType = env.getReturnType();
        IRType.Type actualReturnType = statement.getReturnExpr().getType();
        if (desiredReturnType != actualReturnType){
            if (desiredReturnType == IRType.Type.VOID){
                notifyError("Attempted to return value from a void function.", statement);
            }
            else {
                notifyError("Attempted to return value of type " + actualReturnType.toString() +
                " from function of type " + desiredReturnType.toString() + ".", statement);
            }
        }
    }

    private void checkIRMethodCallExpression(IRMethodCallExpression expr){
        // 5, 6
        IRMethodDecl md = expr.getIRMethodDecl();
        if (md.getReturnType() == IRType.Type.VOID) {
            notifyError("Expression uses return value of void method.", expr);
        }
        List<IRMemberDecl> parameters = md.getParameters().getVariableList();
        List<IRExpression> arguments = expr.getArguments();
        if (parameters.size() != arguments.size()) {
            notifyError("Method " + md.getName() + " called with " + arguments.size() +
            " parameters; needs " + parameters.size() + ".", expr);
        }
        for (int i = 0; i < parameters.size(); i++) {
            IRType.Type parType = parameters.get(i).getType();
            IRType.Type argType = arguments.get(i).getType();
            if (parType != argType) {
                notifyError("Method " + md.getName() + "requires parameter " + parameters.get(i).getName() +
                " to have type " + parType.toString() + ", but got type " + argType.toString(), expr);
            }
        }
    }

    private void checkIRVariableExpression(IRVariableExpression expr){
        // TODO
    }

      // TODO probably lots more of these?

}

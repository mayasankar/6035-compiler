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

// write semantic checks 1,2,4,7,10,11,14,18,19,20
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
        env.popMethodTable();
        env.popVariableTable();
        env.popReturnType();
    }

    private void notifyError(String error, IRNode problematicNode){
        System.out.println("ERROR " + problematicNode.location() + ": " + error);
    }

    // ------- PROGRAM HELPER CHECKS ----------

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

    // ------- TABLE CHECKS ----------

    private void checkMethodTable(MethodTable table){
        // TODO
    }

    private void checkVariableTable(VariableTable table){
        // TODO
    }

    private void checkClassTable(ClassTable table) {
        // not actually todo because we don't need to do classes
    }

    // ------- DECL CHECKS ----------

    private void checkIRFieldDecl(IRFieldDecl param) {
        // low priority because we don't seem to be using this?
    }

    private void checkIRLocalDecl(IRLocalDecl param) {
        // low priority because we don't seem to be using this?
    }

    private void checkIRMemberDecl(IRMemberDecl variable){
        // TODO
    }

    private void checkIRMethodDecl(IRMethodDecl method){
        // TODO
    }

    private void checkIRParameterDecl(IRParameterDecl param) {
        // low priority because we don't seem to be using this?
    }

    // ------- EXPRESSION CHECKS ----------

    private void checkIRBinaryOpExpression(IRBinaryOpExpression expr) {
        // 16, part of 15, part of 17
        IRExpression left = expr.getLeftExpr();
        checkIRExpression(left);
        IRExpression right = expr.getRightExpr();
        checkIRExpression(right);
        Token opToken = expr.getOperator();
        String op = opToken.getText();
        if (op == "+" || op == "-" || op == "*" || op == "/" || op == "%" || op == "<"
            || op == "<=" || op == ">" || op == ">=") {
            if (left.getType() != IRType.Type.INT) {
                notifyError("First argument to operator " + op + " must be of type INT.", left);
            }
            if (right.getType() != IRType.Type.INT) {
                notifyError("Second argument to operator " + op + " must be of type INT.", right);
            }
        }
        if (op == "&&" || op == "||") {
            if (left.getType() != IRType.Type.BOOL) {
                notifyError("First argument to operator " + op + " must be of type BOOL.", left);
            }
            if (right.getType() != IRType.Type.BOOL) {
                notifyError("Second argument to operator " + op + " must be of type BOOL.", right);
            }
        }
        if (op == "==" || op == "!=") {
            if (left.getType() != right.getType()) {
                notifyError("Cannot compare types " + left.getType() + " and " +
                right.getType() + " with operator " + op + ".", right);
            }
            if (left.getType() != IRType.Type.INT && left.getType() != IRType.Type.BOOL) {
                notifyError("Can only compare objects of type INT or BOOL with operator " + op + ".", left);
            }
        }
    }

    private void checkIRExpression(IRExpression expr) {
        // TODO
    }

    private void checkIRLenExpression(IRLenExpression expr) {
        // 12
        String argumentName = expr.getArgument();
        VariableTable lookupTable = env.getVariableTable();
        IRMemberDecl argument = lookupTable.get(argumentName);
        if (argument == null) {
            notifyError("Cannot apply len() to uninstantiated variable '" + argumentName + "'.", expr);
        }
        else if (argument.getType() != IRType.Type.INT_ARRAY && argument.getType() != IRType.Type.BOOL_ARRAY) {
            notifyError("Cannot apply len() to non-array-type variable '" + argumentName + "'.", expr);
        }
    }

    private void checkIRMethodCallExpression(IRMethodCallExpression expr) {
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

    private void checkIRTernaryOpExpression(IRTernaryOpExpression expr) {
        // TODO
    }

    private void checkIRUnaryOpExpression(IRUnaryOpExpression expr) {
        // part of 15, part of 17
        IRExpression arg = expr.getArgument();
        checkIRExpression(arg);
        Token opToken = expr.getOperator();
        String op = opToken.getText();
        if (op == "-") {
            if (arg.getType() != IRType.Type.INT) {
                notifyError("Argument for unary minus must be of type int.", arg);
            }
        }
        if (op == "!") {
            if (arg.getType() != IRType.Type.BOOL) {
                notifyError("Argument for ! operator must be of type bool.", arg);
            }
        }
    }

    private void checkIRVariableExpression(IRVariableExpression expr) {
        // TODO
    }

    // ------- STATEMENT CHECKS ----------

    private void checkIRAssignStatement(IRAssignStatement statement) {
        // TODO
    }

    private void checkIRBlock(IRBlock block){
      for (IRStatement s : block.getStatements()){
        checkIRStatement(s);
      }
    }

    private void checkIRForStatement(IRForStatement statement) {
        // 21
        // TODO add scope to stack
        // TODO forloops need to store the assigned variable somewhere, like in the block scope or a parent of it
        checkIRAssignStatement(statement.getStepFunction());
        checkIRAssignStatement(statement.getInitializer());
        IRExpression cond = statement.getCondition();
        checkIRExpression(cond);
        if (cond.getType() != IRType.Type.BOOL){
            notifyError("For loop condition expression must have type bool.", cond);
        }
        checkIRBlock(statement.getBlock());
    }

    private void checkIRIfStatement(IRIfStatement statement) {
        // part of 13
    	IRBlock thenBlock = statement.getThenBlock();
        checkIRBlock(thenBlock);
    	IRBlock elseBlock = statement.getElseBlock();
        checkIRBlock(elseBlock);
        IRExpression cond = statement.getCondition();
        checkIRExpression(cond);
        if (cond.getType() != IRType.Type.BOOL){
            notifyError("If statement condition expression must have type bool.", cond);
        }

    }

    private void checkIRMethodCallStatement(IRMethodCallStatement statement) {
        // TODO
    }

    private void checkIRReturnStatement(IRReturnStatement statement){
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

    private void checkIRStatement(IRStatement statement){
        // TODO
    }

    private void checkIRWhileStatement(IRWhileStatement statement) {
        // part of 13
        checkIRBlock(statement.getBlock());
        IRExpression cond = statement.getCondition();
        checkIRExpression(cond);
        if (cond.getType() != IRType.Type.BOOL){
            notifyError("While statement condition expression must have type bool.", cond);
        }
    }

}

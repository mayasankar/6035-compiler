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

// write semantic checks 2
// test  semantic checks 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21

public class SemanticChecker {

    private EnvStack env = new EnvStack();
    private boolean hasError = false;

    public boolean checkProgram(IRProgram tree){
        //System.out.println("Debugging: starts checkProgram().");
        notifyError("DEBUG: Checking program for semantic errors.", tree);
        env.push(tree.methods);
        env.push(tree.fields);
        env.push(IRType.Type.VOID);
        checkImportsAndGlobals(tree.imports, tree.fields);
        checkVariableTable(tree.fields);
        checkMethodTable(tree.methods);
        checkHasMain(tree);
        env.popMethodTable();
        env.popVariableTable();
        env.popReturnType();
        return hasError;
    }

    private void notifyError(String error, IRNode problematicNode){
        hasError = true;
        System.err.println("ERROR " + problematicNode.location() + ": " + error);
    }

    // ------- PROGRAM HELPER CHECKS ----------

    private void checkHasMain(IRProgram program){
        // 3
        IRMethodDecl mainMethod = program.methods.get("main");
        if (mainMethod == null) {
            notifyError("Program has no main method.", program);
            return;
        }
        if (mainMethod.getReturnType() != IRType.Type.VOID){
            notifyError("Main method return type is not void.", mainMethod);
        }
        if (! mainMethod.getParameters().isEmpty()){
            notifyError("Main method requires input parameters.", mainMethod);
        }
    }

    private void checkImportsAndGlobals(List<IRImportDecl> imports, VariableTable varTable){
        // first part of 1
        // check that they're all distinct
        List<IRMemberDecl> variables = varTable.getVariableList();
        HashSet<String> namesSet = new HashSet<>();
        for (IRImportDecl imp : imports){
            if (namesSet.contains(imp.getName())){
                notifyError("Attempted to import an identifier previously imported.", imp);
            }
            namesSet.add(imp.getName());
        }
        for (IRMemberDecl imp : variables){
            if (namesSet.contains(imp.getName())){
                notifyError("Attempted to declare a global variable that conflicts with an import name.", imp);
            }
            namesSet.add(imp.getName());
        }
    }

    // ------- TABLE CHECKS ----------

    private void checkMethodTable(MethodTable table){
        // part of 1
        //System.out.println("Debugging: called checkMethodTable().");
        List<IRMethodDecl> methods = table.getMethodList();
        HashSet<String> methodsSet = new HashSet<>();
        for (IRMethodDecl met : methods){
            //System.out.println("Debugging: methods=" + methods.toString());
            if (methodsSet.contains(met.getName())){
                 notifyError("Attempted to declare method " + met.getName() +
                 " but a method of that name already exists in the same scope.", met);
            }
            checkIRMethodDecl(met);
            //System.out.println("Debugging: checked IRMethodDecl " + met.toString());
            methodsSet.add(met.getName());
        }
    }

    private void checkVariableTable(VariableTable table){
        // part of 1
         List<IRMemberDecl> variables = table.getVariableList();
         //System.out.println("Debugging: called checkVariableTable(). variables=" + variables.toString());
         HashSet<String> variablesSet = new HashSet<>();
         for (IRMemberDecl var : variables){
             //System.out.println("Debugging: variablesSet=" + variablesSet.toString());
             if (variablesSet.contains(var.getName())){
                 notifyError("Attempted to declare variable " + var.getName() +
                 " but a variable of that name already exists in the same scope.", var);
             }
             checkIRMemberDecl(var);
             variablesSet.add(var.getName());
         }
    }

    private void checkClassTable(ClassTable table) {
        System.err.println("Unimplemented method checkClassTable() should never be called.");
    }

    // ------- DECL CHECKS ----------

    private void checkIRFieldDecl(IRFieldDecl param) {
        // low priority because we don't seem to be using this?
        System.err.println("Unimplemented method checkIRFieldDecl() should never be called.");
    }

    private void checkIRLocalDecl(IRLocalDecl param) {
        // low priority because we don't seem to be using this?
        System.err.println("Unimplemented method checkIRLocalDecl() should never be called.");
    }

    private void checkIRMemberDecl(IRMemberDecl variable) {
        // 4
        IRType.Type type = variable.getType();
        int length = variable.getLength();
        if (type == IRType.Type.BOOL_ARRAY || type == IRType.Type.INT_ARRAY) {
            if (length == 0) {
                notifyError("Cannot declare an array of size 0.", variable);
            }
        }
        else {
            if (length != 0) {
                notifyError("Something has gone seriously wrong in the compiler," +
                "and a variable thinks it's an array. This should never happen.", variable);
            }
        }
    }

    private void checkIRMethodDecl(IRMethodDecl method){
        // 7
        IRType.Type returnType = method.getReturnType();
        VariableTable parameters = method.getParameters();
        //System.out.println("Debugging: called checkIRMethodDecl(). parameters=" + parameters.toString());

        env.push(parameters);
        env.push(returnType);
        IRBlock code = method.getCode();
        checkIRBlock(code);
        env.popVariableTable();
        env.popReturnType();

        checkVariableTable(parameters);
        for (IRMemberDecl param : parameters.getVariableList()) {
            if (param.getType() != IRType.Type.BOOL && param.getType() != IRType.Type.INT) {
                notifyError("Parameter " + param.getName() + " for method " + method.getName() +
                " is not of type int or bool.", param);
            }
        }
        if (returnType != IRType.Type.BOOL && returnType != IRType.Type.INT && returnType != IRType.Type.VOID) {
            notifyError("Return type for method " + method.getName() + " is not int, bool, or void.", method);
        }
    }

    private void checkIRParameterDecl(IRParameterDecl param) {
        // low priority because we don't seem to be using this?
        System.err.println("Unimplemented method checkIRParameterDecl() should never be called.");
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
        if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") || op.equals("%") || op.equals("<")
            || op.equals("<=") || op.equals(">") || op.equals(">=")) {
            if (left.getType() != IRType.Type.INT) {
                notifyError("First argument to operator " + op + " must be of type INT.", left);
            }
            if (right.getType() != IRType.Type.INT) {
                notifyError("Second argument to operator " + op + " must be of type INT.", right);
            }
        }
        else if (op.equals("&&") || op.equals("||")) {
            if (left.getType() != IRType.Type.BOOL) {
                notifyError("First argument to operator " + op + " must be of type BOOL.", left);
            }
            if (right.getType() != IRType.Type.BOOL) {
                notifyError("Second argument to operator " + op + " must be of type BOOL.", right);
            }
        }
        else if (op.equals("==") || op.equals("!=")) {
            if (left.getType() != right.getType()) {
                notifyError("Cannot compare types " + left.getType() + " and " +
                right.getType() + " with operator " + op + ".", right);
            }
            if (left.getType() != IRType.Type.INT && left.getType() != IRType.Type.BOOL) {
                notifyError("Can only compare objects of type INT or BOOL with operator " + op + ".", left);
            }
        }
        else {
            System.err.println("checkIRBinaryOpExpression() semantic checking error: operator '" + op + "' did not match any of accepted types.");
        }
    }

    private void checkIRExpression(IRExpression expr) {
        switch(expr.getExpressionType()) {
          case BOOL_LITERAL: case INT_LITERAL: case STRING_LITERAL: {
            return;
          } case UNARY: {
            checkIRUnaryOpExpression((IRUnaryOpExpression) expr); break;
          } case BINARY: {
            checkIRBinaryOpExpression((IRBinaryOpExpression) expr); break;
          } case TERNARY: {
            checkIRTernaryOpExpression((IRTernaryOpExpression) expr); break;
          } case LEN: {
            checkIRLenExpression((IRLenExpression) expr); break;
          } case METHOD_CALL: {
            checkIRMethodCallExpression((IRMethodCallExpression) expr); break;
          } case VARIABLE: {
            checkIRVariableExpression((IRVariableExpression) expr); break;
          } default: {
            notifyError("IR error: UNSPECIFIED expr", expr);
          }
        }
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
        // 14
        IRExpression trueExpr = expr.getTrueExpression();
        checkIRExpression(trueExpr);
        IRExpression falseExpr = expr.getFalseExpression();
        checkIRExpression(falseExpr);
        IRExpression cond = expr.getCondition();
        checkIRExpression(cond);
        if (cond.getType() != IRType.Type.BOOL){
            notifyError("Ternary operator condition expression must have type bool.", cond);
        }
        if (trueExpr.getType() != falseExpr.getType()) {
            notifyError("Ternary operator should return same type in true or false cases.", expr);
        }
    }

    private void checkIRUnaryOpExpression(IRUnaryOpExpression expr) {
        // part of 15, part of 17
        IRExpression arg = expr.getArgument();
        checkIRExpression(arg);
        Token opToken = expr.getOperator();
        String op = opToken.getText();
        if (op.equals("-")) {
            if (arg.getType() != IRType.Type.INT) {
                notifyError("Argument for unary minus must be of type int.", arg);
            }
        }
        if (op.equals("!")) {
            if (arg.getType() != IRType.Type.BOOL) {
                notifyError("Argument for ! operator must be of type bool.", arg);
            }
        }
    }

    private void checkIRVariableExpression(IRVariableExpression var) {
        // 10, 11
        VariableTable table = env.getVariableTable();
        IRMemberDecl decl = table.get(var.getName());
        IRExpression idxExpr = var.getIndexExpression();
        if (decl == null) {
          notifyError("Reference to undeclared variable '" + var.getName() + "'.", var);
        } else if (idxExpr != null) {
          IRType.Type declType = decl.getType();
          if (declType != IRType.Type.INT_ARRAY && declType != IRType.Type.BOOL_ARRAY) {
            notifyError("Cannot index into non-array variable '" + var.getName() + "'.", var);
          }
        }
        if (idxExpr != null) {
          IRType.Type exprType = idxExpr.getType();
          if (exprType != IRType.Type.INT) {
            notifyError("Array index must be an integer.", idxExpr);
          }
        }

    }

    // ------- STATEMENT CHECKS ----------

    private void checkIRAssignStatement(IRAssignStatement statement) {
        // 18, 19
        IRVariableExpression varAssigned = statement.getVarAssigned();
        checkIRVariableExpression(varAssigned);
        String op = statement.getOperator();
        IRExpression value = statement.getValue();
        if (op != "++" && op != "--"){
            checkIRExpression(value);
        }

        String varName = varAssigned.getName();
        IRExpression arrayIndex = varAssigned.getIndexExpression();
        VariableTable lookupTable = env.getVariableTable();
        IRMemberDecl assignee = lookupTable.get(varName);
        if (assignee == null) {
            notifyError("Cannot assign to undeclared variable '" + varName + "'.", varAssigned);
            return;
        }
        if (op.equals("=")) {
            if (arrayIndex == null) {
                // we should have an int or bool, not an array
                if (assignee.getType() != value.getType()) {
                    notifyError("Cannot assign a value of type " + value.getType().toString() +
                    " to a variable of type " + assignee.getType() + ".", value);
                }
                if (assignee.getType() != IRType.Type.INT && assignee.getType() != IRType.Type.BOOL) {
                    notifyError("Cannot assign to variable '" + varName + "' of type " + assignee.getType().toString() +
                    ".", varAssigned);
                }
            }
            else {
                // we should have an array; checkIRVariableExpression handles checking that the thing being indexed to is an array
                // TODO arkadiy wants to refactor type to have ARRAY as its own type
                if (!(assignee.getType() != IRType.Type.INT_ARRAY && assignee.getType() != IRType.Type.BOOL_ARRAY) && // the thing we're trying to assign to is in fact an array
                   !((assignee.getType() == IRType.Type.INT_ARRAY && value.getType() == IRType.Type.INT) // it's not the case that we're putting an int in an int_array
                   || (assignee.getType() == IRType.Type.BOOL_ARRAY && value.getType() == IRType.Type.BOOL))) { // it's also not the case that we're putting a bool in a bool_array
                       notifyError("Cannot assign a value of type " + value.getType().toString() +
                       " to an array location of type " + assignee.getType() + ".", value);
                   }
            }
        }
        else if (op.equals("+=") || op.equals("-=") || op.equals("++") || op.equals("--")) {
            //System.out.println("DEBUG: this branch! op=" + op);
            if ((op.equals("+=") || op.equals("-=")) && value.getType() != IRType.Type.INT) {
                notifyError("Cannot increment by a value of type " + value.getType().toString() + ".", value);
            }
            if (arrayIndex == null) {
                // we should have an int or bool, not an array
                if (assignee.getType() != IRType.Type.INT) {
                    notifyError("Cannot increment a variable of type " + assignee.getType().toString() + ".", assignee);
                }
            }
            else {
                // we should have an array
                if (assignee.getType() != IRType.Type.INT_ARRAY && assignee.getType() != IRType.Type.BOOL_ARRAY) {
                    notifyError("Cannot index into non-array-type variable '" + varName + "'.", varAssigned);
                }
                if (assignee.getType() != IRType.Type.INT_ARRAY) {
                    notifyError("Cannot increment a variable of type " + assignee.getType().toString() + ".", assignee);
                }
            }
        }
        else {
            System.err.println("checkIRAssignStatement() semantic checking error: statement operator '" + op + "' did not match any of accepted types.");
        }
    }

    private void checkIRBlock(IRBlock block){
      env.push(block.getFields());
      checkVariableTable(block.getFields());
      // TODO arkadiy wants to fix the fact that there isn't a direct getVariableList method from IRBlock
      for (IRStatement s : block.getStatements()){
        checkIRStatement(s);
      }
      env.popVariableTable();
    }

    private void checkIRForStatement(IRForStatement statement) {
        // 21
        env.push(statement);
        checkIRAssignStatement(statement.getStepFunction());
        checkIRAssignStatement(statement.getInitializer());
        IRExpression cond = statement.getCondition();
        checkIRExpression(cond);
        if (cond.getType() != IRType.Type.BOOL){
            notifyError("For loop condition expression must have type bool.", cond);
        }
        checkIRBlock(statement.getBlock());
        env.popLoopStatement();
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
        checkIRMethodCallExpression(statement.getMethodCall());
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
        switch(statement.getStatementType()) {
          case ASSIGN_EXPR: {
            checkIRAssignStatement((IRAssignStatement) statement); break;
          } case METHOD_CALL: {
            checkIRMethodCallStatement((IRMethodCallStatement) statement); break;
          } case IF_BLOCK: {
            checkIRIfStatement((IRIfStatement) statement); break;
          } case FOR_BLOCK: {
            checkIRForStatement((IRForStatement) statement); break;
          } case WHILE_BLOCK: {
            checkIRWhileStatement((IRWhileStatement) statement); break;
          } case RETURN_EXPR: {
            checkIRReturnStatement((IRReturnStatement) statement); break;
          } case BREAK: case CONTINUE: {
            checkIRLoopStatement((IRLoopStatement) statement); break;
          } default: {
            notifyError("IR error: UNSPECIFIED statement", statement);
          }
        }
    }

    private void checkIRWhileStatement(IRWhileStatement statement) {
        // part of 13
        env.push(statement);
        checkIRBlock(statement.getBlock());
        IRExpression cond = statement.getCondition();
        checkIRExpression(cond);
        if (cond.getType() != IRType.Type.BOOL){
            notifyError("While statement condition expression must have type bool.", cond);
        }
        env.popLoopStatement();
    }

    private void checkIRLoopStatement(IRLoopStatement statement) {
      IRStatement loop = env.getLoopStatement();
      if (loop == null) {
        notifyError(
          statement.getStatementType().name().toLowerCase() +
          "statement not within a while or for loop",
          statement
        );
      } else {
        statement.setLoop(loop);
      }
    }

}

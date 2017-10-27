package edu.mit.compilers.trees;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.math.BigInteger;

import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.operator.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;

// write semantic checks 2
// test  semantic checks 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21

public class SemanticChecker {
    //Should there be an errorStream and outputStream associated with this class?

    private EnvStack env = new EnvStack();
    private boolean hasError = false;

    public boolean checkProgram(IRProgram tree){
        // System.out.println("Debugging: starts checkProgram().");
        // notifyError("DEBUG: Checking program for semantic errors.", tree);
        env.push(tree.getMethodTable());
        env.push(tree.getVariableTable());
        env.push(IRType.Type.VOID);
        checkGlobals(tree.getVariableTable(), tree.getMethodTable());
        checkVariableTable(tree.getVariableTable());
        checkMethodTable(tree.getMethodTable());
        checkHasMain(tree);
        env.popMethodTable();
        env.popVariableTable();
        env.popReturnType();
        // if (! hasError) { // maybe keep track of how many errors we've found?
        //     System.err.println("No semantic errors found :)");
        // }
        return hasError;
    }

    private void notifyError(String error, IRNode problematicNode){
        hasError = true;
        System.err.println("ERROR " + problematicNode.location() + ": " + error);
    }

    // ------- PROGRAM HELPER CHECKS ----------

    private void checkHasMain(IRProgram program){
        // 3
        IRMethodDecl mainMethod = program.getMethodTable().get("main");
        if (mainMethod == null) {
            notifyError("Program has no main method.", program);
            return;
        }
        if (mainMethod.isImport()) {
            notifyError("Cannot import main.", program);
            return;
        }
        if (mainMethod.getReturnType() != IRType.Type.VOID){
            notifyError("Main method return type is not void.", mainMethod);
        }
        if (! mainMethod.getParameters().isEmpty()){
            notifyError("Main method cannot have parameters.", mainMethod);
        }
    }

    private void checkGlobals(VariableTable varTable, MethodTable methodTable){
        // first part of 1
        // check that they're all distinct
        List<IRMemberDecl> variables = varTable.getVariableList();
        List<IRMethodDecl> methods = methodTable.getMethodList();
        HashSet<String> methodsSet = new HashSet<>();
        for (IRMethodDecl met : methods) {
          methodsSet.add(met.getName());
        }
        for (IRMemberDecl var : variables){
            if (methodsSet.contains(var.getName())){
                notifyError("Attempted to declare variable and function of the same name.", var);
            }
        }
    }

    private void checkIntLiteral(IRIntLiteral il) {
        if (il.getValue().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            notifyError("Attempted to assign integer too large for 64-bit int.", il);
        } else if (il.getValue().compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
            notifyError("Attempted to assign integer less than minimum for 64-bit int.", il);
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
            // it would be nice if we could
            if (methodsSet.contains(met.getName())){
                 notifyError("Attempted to declare method " + met.getName() +
                 " but a method of that name already exists in the same scope.", met);
            }
            if (!met.isImport()) {
                checkIRMethodDecl(met);
            }
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
        int length = variable.getLength();
        if (variable.isArray()) {
            if (length <= 0) {
                notifyError("Cannot declare an array of size " + length, variable);
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
            if (code.getVariableTable().get(param.getName()) != parameters.get(param.getName())) {
            	notifyError("Parameter " + param.getName() + " for method " + method.getName() +
    			 " is shadowed by a local variable.", code.getVariableTable().get(param.getName()).getDecl());
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
          case BOOL_LITERAL: case STRING_LITERAL: {
            return;
        } case INT_LITERAL: {
            checkIntLiteral((IRIntLiteral) expr); break;
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
        VariableDescriptor argument = lookupTable.get(argumentName);
        if (argument == null) {
            notifyError("Cannot apply len() to undeclared variable '" + argumentName + "'.", expr);
        }
        else if (argument.getType() != IRType.Type.INT_ARRAY && argument.getType() != IRType.Type.BOOL_ARRAY) {
            notifyError("Cannot apply len() to non-array-type variable '" + argumentName + "'.", expr);
        }
    }

    private void checkIRMethodCallExpression(IRMethodCallExpression expr) {
      checkIRMethodCallExpression(expr, true);
    }

    private void checkIRMethodCallExpression(IRMethodCallExpression expr, boolean isInExpression) {
        // 5, 6
        VariableTable table = env.getVariableTable();
        VariableDescriptor desc = table.get(expr.getName());
        if (desc != null) {
            notifyError(expr.getName() + " is most recently declared as a method, not a function", expr);
            return;
        }

        MethodTable lookupTable = env.getMethodTable();
        IRMethodDecl md = lookupTable.get(expr.getName());
        if (md == null) {
            notifyError("Calls undefined method '" + expr.getName() + "'.", expr);
            return;
        } else if (! expr.comesAfter(md)) {
            notifyError("Calls method " + expr.getName() + " before method is declared.", expr);
            return;
        }

        // NOTE: the following line means we're not actually completing the
        // IRMethodCallExpressions until we do semantic checking when we change things
        expr.setType(md.getReturnType());

        if (isInExpression && md.getReturnType() == IRType.Type.VOID) {
            notifyError("Expression uses method with return value of void.", expr);
        }

        List<IRExpression> arguments = expr.getArguments();
        // == CODE TO CHECK PARAMETER LISTS ARE THE SAME
        if (!md.isImport()) { // don't check imports match parameter lengths
            List<IRMemberDecl> parameters = md.getParameters().getVariableList();
            if (parameters.size() != arguments.size()) {
                notifyError("Method " + md.getName() + " called with " + arguments.size() +
                " parameters; needs " + parameters.size() + ".", expr);
            }
            for (int i = 0; i < parameters.size() && i < arguments.size(); i++) {
                checkIRExpression(arguments.get(i));
                IRType.Type parType = parameters.get(i).getType();
                IRType.Type argType = arguments.get(i).getType();
                if (parType != argType) {
                    notifyError("Method " + md.getName() + " requires parameter " + parameters.get(i).getName() +
                    " to have type " + parType.toString() + ", but got type " + argType.toString(), expr);
                }
            }
        } else {
            for (IRExpression arg : arguments) {
                checkIRExpression(arg); // useful to initialize types for that expression
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
        VariableDescriptor desc = table.get(var.getName());
        IRExpression idxExpr = var.getIndexExpression();

        if (desc == null) {
            notifyError("Reference to undeclared variable '" + var.getName() + "'.", var);
            return;
        }

        IRMemberDecl decl = desc.getDecl();
        // TODO (mayars) -- why do we have this here?
        checkIRMemberDecl(decl);

        if (idxExpr != null) {
            checkIRExpression(idxExpr);
            IRType.Type declType = decl.getType();
            if (declType != IRType.Type.INT_ARRAY && declType != IRType.Type.BOOL_ARRAY) {
                notifyError("Cannot index into non-array variable '" + var.getName() + "'.", var);
            }
        }
        if (idxExpr == null) {
            var.setType(decl.getType()); // NOTE: this means we're not actually completing the IRVariableExpressions until we do semantic checking when we change things
        }
        else {
            if (decl.getType() == IRType.Type.INT_ARRAY) {
                var.setType(IRType.Type.INT);
            }
            else if (decl.getType() == IRType.Type.BOOL_ARRAY) {
                var.setType(IRType.Type.BOOL);
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
        if (!op.equals("++") && !op.equals("--")){
            checkIRExpression(value);
            // if (value.getType() == null) { // happens when value is an undeclared variable
            //     System.out.println("Debugging: null value.getType(). value=" + value);
            // }
        }

        String varName = varAssigned.getName();
        IRExpression arrayIndex = varAssigned.getIndexExpression();
        if (arrayIndex != null){
            checkIRExpression(arrayIndex);
        }
        VariableTable lookupTable = env.getVariableTable();
        VariableDescriptor assigneeDesc = lookupTable.get(varName);
        if (assigneeDesc == null) {
            notifyError("Cannot assign to undeclared variable '" + varName + "'.", varAssigned);
            return;
        }
        IRMemberDecl assignee = assigneeDesc.getDecl();
        if (op.equals("=")) {
            if (arrayIndex == null) {
                // we should have an int or bool, not an array
                if (assignee.getType() != value.getType()) {
                    notifyError("Cannot assign a value of type " + value.getType() +
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
                       " to a location in an " + assignee.getType() + ".", value);
                   }
            }
        }
        else if (op.equals("+=") || op.equals("-=") || op.equals("++") || op.equals("--")) {
            //System.out.println("DEBUG: this branch! op=" + op);
            if ((op.equals("+=") || op.equals("-=")) && value.getType() != IRType.Type.INT) {
                notifyError("Cannot increment by a value of type " + value.getType() + ".", value);
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
        if(elseBlock != null) {
            checkIRBlock(elseBlock);
        }
        IRExpression cond = statement.getCondition();
        checkIRExpression(cond);
        if (cond.getType() != IRType.Type.BOOL){
            notifyError("If statement condition expression must have type bool.", cond);
        }

    }

    private void checkIRMethodCallStatement(IRMethodCallStatement statement) {
        checkIRMethodCallExpression(statement.getMethodCall(), false);
    }

    private void checkIRReturnStatement(IRReturnStatement statement){
        // 8, 9
        IRType.Type desiredReturnType = env.getReturnType();
        if (! statement.isVoid()) {
            checkIRExpression(statement.getReturnExpr());
        }
        IRType.Type actualReturnType = statement.getReturnType();
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

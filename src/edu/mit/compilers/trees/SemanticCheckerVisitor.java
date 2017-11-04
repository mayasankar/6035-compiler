package edu.mit.compilers.trees;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import antlr.Token;
import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRProgram;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.IRType.Type;
import edu.mit.compilers.ir.decl.IRFieldDecl;
import edu.mit.compilers.ir.decl.IRLocalDecl;
import edu.mit.compilers.ir.decl.IRMemberDecl;
import edu.mit.compilers.ir.decl.IRMethodDecl;
import edu.mit.compilers.ir.decl.IRParameterDecl;
import edu.mit.compilers.ir.expression.IRBinaryOpExpression;
import edu.mit.compilers.ir.expression.IRExpression;
import edu.mit.compilers.ir.expression.IRLenExpression;
import edu.mit.compilers.ir.expression.IRMethodCallExpression;
import edu.mit.compilers.ir.expression.IRTernaryOpExpression;
import edu.mit.compilers.ir.expression.IRUnaryOpExpression;
import edu.mit.compilers.ir.expression.IRVariableExpression;
import edu.mit.compilers.ir.expression.literal.IRLiteral;
import edu.mit.compilers.ir.statement.IRAssignStatement;
import edu.mit.compilers.ir.statement.IRBlock;
import edu.mit.compilers.ir.statement.IRForStatement;
import edu.mit.compilers.ir.statement.IRIfStatement;
import edu.mit.compilers.ir.statement.IRLoopStatement;
import edu.mit.compilers.ir.statement.IRMethodCallStatement;
import edu.mit.compilers.ir.statement.IRReturnStatement;
import edu.mit.compilers.ir.statement.IRStatement;
import edu.mit.compilers.ir.statement.IRWhileStatement;
import edu.mit.compilers.symbol_tables.MethodTable;
import edu.mit.compilers.symbol_tables.VariableDescriptor;
import edu.mit.compilers.symbol_tables.VariableTable;

public class SemanticCheckerVisitor implements IRNode.IRNodeVisitor<Boolean> {

	private EnvStack env = new EnvStack();
	private Set<String> globalNamesSet = new HashSet<>();
	private boolean hasError = false;

    private void notifyError(String error, IRNode problematicNode){
        hasError = true;
        System.err.println("ERROR " + problematicNode.location() + ": " + error);
    }

	@Override
	public Boolean on(IRProgram tree) {
        env.push(tree.getMethodTable());
        env.push(tree.getVariableTable());
        env.push(IRType.Type.VOID);
        checkVariableTable(tree.getVariableTable());
        checkHasMain(tree);
        for(IRMethodDecl methods: tree.getMethodTable().getMethodList()) {
        	methods.accept(this);
        }
        for(IRMemberDecl variable: tree.getVariableTable().getVariableList()) {
        	variable.accept(this);
        }
        env.popMethodTable();
        env.popVariableTable();
        env.popReturnType();

        return hasError;
	}

    private void checkVariableTable(VariableTable table){
         List<IRMemberDecl> variables = table.getVariableList();
         HashSet<String> variablesSet = new HashSet<>();
         for (IRMemberDecl var : variables){
             if (variablesSet.contains(var.getName())){
                 notifyError("Attempted to declare variable " + var.getName() +
                 " but a variable of that name already exists in the same scope.", var);
             }
             variablesSet.add(var.getName());
         }
    }

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

	@Override
	public Boolean on(IRMethodDecl method) {
		String name = method.getName();
        IRType.Type returnType = method.getReturnType();
        VariableTable parameters = method.getParameters();
		if(globalNamesSet.contains(name)) {
			notifyError("Attempted to declare method " + name +
	                 " but a method of that name already exists in the same scope.", method);
		} else {
			globalNamesSet.add(name);
		}
        if (Arrays.asList(IRType.Type.BOOL, IRType.Type.INT, IRType.Type.VOID).contains(returnType)) {
            notifyError("Return type for method " + name + " is not int, bool, or void.", method);
        }

        env.push(parameters);
        env.push(returnType);
        IRBlock code = method.getCode();
        code.accept(this);
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
            param.accept(this);
        }

		return hasError;
	}

	@Override
	public Boolean on(IRUnaryOpExpression expr) { // TYPES
        // part of 15, part of 17
        IRExpression arg = expr.getArgument();
        arg.accept(this);
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

        return hasError;
	}

	@Override
	public Boolean on(IRBinaryOpExpression expr) { // TYPES
        // 16, part of 15, part of 17
        IRExpression left = expr.getLeftExpr();
        IRExpression right = expr.getRightExpr();

        left.accept(this);
        right.accept(this);

        String op = expr.getOperator().getText();
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

        return hasError;
	}

	@Override
	public Boolean on(IRTernaryOpExpression expr) {
        // 14
        IRExpression trueExpr = expr.getTrueExpression();
        IRExpression falseExpr = expr.getFalseExpression();
        IRExpression cond = expr.getCondition();

        cond.accept(this);
        trueExpr.accept(this);
        falseExpr.accept(this);

        if (cond.getType() != IRType.Type.BOOL){
            notifyError("Ternary operator condition expression must have type bool.", cond);
        }
        if (trueExpr.getType() != falseExpr.getType()) {
            notifyError("Ternary operator should return same type in true or false cases.", expr);
        }

        return hasError;
	}

	@Override
	public Boolean on(IRLenExpression expr) { // TYPES
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

        return hasError;
	}

	@Override
	public Boolean on(IRVariableExpression var) {
        // 10, 11
        VariableDescriptor desc = env.getVariableTable().get(var.getName());
        IRExpression idxExpr = var.getIndexExpression();

        if (desc == null) {
            notifyError("Reference to undeclared variable '" + var.getName() + "'.", var);
            return hasError;
        }

        if (idxExpr != null) {
            idxExpr.accept(this);
            IRType.Type declType = desc.getType();
            if (declType != IRType.Type.INT_ARRAY && declType != IRType.Type.BOOL_ARRAY) {
                notifyError("Cannot index into non-array variable '" + var.getName() + "'.", var);
            }

            IRType.Type exprType = idxExpr.getType();
            if (exprType != IRType.Type.INT) {
                notifyError("Array index must be an integer.", idxExpr);
            }
        }

        return hasError;
	}

	@Override
	public Boolean on(IRMethodCallExpression expr) {
        // 5, 6
        VariableTable table = env.getVariableTable();
        VariableDescriptor desc = table.get(expr.getName());
        if (desc != null) {
            notifyError(expr.getName() + " is most recently declared as a method, not a function", expr);
            return hasError;
        }

        MethodTable lookupTable = env.getMethodTable();
        IRMethodDecl md = lookupTable.get(expr.getName());
        if (md == null) {
            notifyError("Calls undefined method '" + expr.getName() + "'.", expr);
            return hasError;
        } else if (! expr.comesAfter(md)) {
            notifyError("Calls method " + expr.getName() + " before method is declared.", expr);
            return hasError;
        }

        // NOTE: the following line means we're not actually completing the
        // IRMethodCallExpressions until we do semantic checking when we change things
        /*expr.setType(md.getReturnType());

        if (isInExpression && md.getReturnType() == IRType.Type.VOID) {
            notifyError("Expression uses method with return value of void.", expr);
        }*/

        List<IRExpression> arguments = expr.getArguments();
        // == CODE TO CHECK PARAMETER LISTS ARE THE SAME
        if (!md.isImport()) { // don't check imports match parameter lengths
            List<IRMemberDecl> parameters = md.getParameters().getVariableList();
            if (parameters.size() != arguments.size()) {
                notifyError("Method " + md.getName() + " called with " + arguments.size() +
                " parameters; needs " + parameters.size() + ".", expr);
            }
            for (int i = 0; i < parameters.size() && i < arguments.size(); i++) {
                arguments.get(i).accept(this);
                IRType.Type parType = parameters.get(i).getType();
                IRType.Type argType = arguments.get(i).getType();
                if (parType != argType) {
                    notifyError("Method " + md.getName() + " requires parameter " + parameters.get(i).getName() +
                    " to have type " + parType.toString() + ", but got type " + argType.toString(), expr);
                }
            }
        } else {
            for (IRExpression arg : arguments) {
                arg.accept(this); // useful to initialize types for that expression
            }
        }

        return hasError;
	}

	@Override
	public Boolean on(IRAssignStatement statement) { // TYPES plsplsplspls
	 // 18, 19
        IRVariableExpression varAssigned = statement.getVarAssigned();
        varAssigned.accept(this);
        String op = statement.getOperator();
        IRExpression value = statement.getValue();
        if (!op.equals("++") && !op.equals("--")){
            value.accept(this);
            // if (value.getType() == null) { // happens when value is an undeclared variable
            //     System.out.println("Debugging: null value.getType(). value=" + value);
            // }
        }

        String varName = varAssigned.getName();
        IRExpression arrayIndex = varAssigned.getIndexExpression();
        if (arrayIndex != null){
            arrayIndex.accept(this);
        }
        VariableTable lookupTable = env.getVariableTable();
        VariableDescriptor assigneeDesc = lookupTable.get(varName);
        if (assigneeDesc == null) {
            notifyError("Cannot assign to undeclared variable '" + varName + "'.", varAssigned);
            return hasError;
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
                    notifyError("Cannot assign to variable '" + varName + "' of type " + assignee.getType() +
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
            throw new RuntimeException("checkIRAssignStatement() semantic checking error: statement operator '" + op + "' did not match any of accepted types.");
        }

        return hasError;
	}

	@Override
	public Boolean on(IRBlock block) {
	    env.push(block.getFields());
	    for (IRMemberDecl decl: block.getFieldDecls()) {
            decl.accept(this);
        }
	    checkVariableTable(block.getFields());
	    // TODO arkadiy wants to fix the fact that there isn't a direct getVariableList method from IRBlock
	    for (IRStatement s : block.getStatements()){
	        s.accept(this);
	    }
	    env.popVariableTable();

	    return hasError;
	}

	@Override
	public Boolean on(IRForStatement statement) {
        // 21
        env.push(statement);
        statement.getStepFunction().accept(this);
        statement.getInitializer().accept(this);
        IRExpression cond = statement.getCondition();
        cond.accept(this);
        if (cond.getType() != IRType.Type.BOOL){
            notifyError("For loop condition expression must have type bool.", cond);
        }
        statement.getBlock().accept(this);
        env.popLoopStatement();

        return hasError;
	}

	@Override
	public Boolean on(IRIfStatement statement) {
        // part of 13
        IRBlock thenBlock = statement.getThenBlock();
        thenBlock.accept(this);
        IRBlock elseBlock = statement.getElseBlock();
        if(elseBlock != null) {
            elseBlock.accept(this);
        }
        IRExpression cond = statement.getCondition();
        cond.accept(this);
        if (cond.getType() != IRType.Type.BOOL){
            notifyError("If statement condition expression must have type bool.", cond);
        }

        return hasError;
	}

	@Override
	public Boolean on(IRLoopStatement statement) {
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

        return hasError;
	}

	@Override
	public Boolean on(IRMethodCallStatement statement) {
		statement.getMethodCall().accept(this);
	    return hasError;
	}

	@Override
	public Boolean on(IRReturnStatement statement) {
        // 8, 9
        IRType.Type desiredReturnType = env.getReturnType();
        if (! statement.isVoid()) {
            statement.getReturnExpr().accept(this);
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

        return hasError;
	}

	@Override
	public Boolean on(IRWhileStatement statement) {
        // part of 13
        env.push(statement);
        statement.getBlock().accept(this);
        IRExpression cond = statement.getCondition();
        cond.accept(this);
        if (cond.getType() != IRType.Type.BOOL){
            notifyError("While statement condition expression must have type bool.", cond);
        }
        env.popLoopStatement();

        return hasError;
	}

	@Override
	public Boolean on(IRFieldDecl ir) {
		String name = ir.getName();
		if(globalNamesSet.contains(name)) {
			notifyError("Attempted to declare variable and function of the same name.", ir);
		} else {
			globalNamesSet.add(name);
		}
		return this.onMemberDecl(ir);
	}

	@Override
	public Boolean on(IRLocalDecl ir) {
		return this.onMemberDecl(ir);
	}

	@Override
	public Boolean on(IRParameterDecl ir) {
		return this.onMemberDecl(ir);
	}

	private Boolean onMemberDecl(IRMemberDecl variable) {
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

        return hasError;
	}

    @Override
    public Boolean onBool(IRLiteral<Boolean> ir) {
        return hasError;
    }

    @Override
    public Boolean onString(IRLiteral<String> ir) {
        return hasError;
    }

    @Override
    public Boolean onInt(IRLiteral<BigInteger> il) {
        if (il.getValue().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            notifyError("Attempted to assign integer too large for 64-bit int.", il);
        } else if (il.getValue().compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
            notifyError("Attempted to assign integer less than minimum for 64-bit int.", il);
        }

        return hasError;
    }

}

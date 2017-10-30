package edu.mit.compilers.trees;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import antlr.Token;
import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRProgram;
import edu.mit.compilers.ir.IRType;
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
import edu.mit.compilers.ir.statement.IRWhileStatement;
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

	@Override
	public Boolean on(IRMethodCallExpression ir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Boolean on(IRLiteral<T> ir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean on(IRAssignStatement ir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean on(IRBlock ir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean on(IRForStatement ir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean on(IRIfStatement ir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean on(IRLoopStatement ir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean on(IRMethodCallStatement ir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean on(IRReturnStatement ir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean on(IRWhileStatement ir) {
		// TODO Auto-generated method stub
		return null;
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

}

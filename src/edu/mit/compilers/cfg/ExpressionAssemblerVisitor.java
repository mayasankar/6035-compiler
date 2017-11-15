package edu.mit.compilers.cfg;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.math.BigInteger;

import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;
import edu.mit.compilers.cfg.lines.*;

public class ExpressionAssemblerVisitor implements IRExpression.IRExpressionVisitor<String> {

    private VariableStackAssigner stacker;

    public ExpressionAssemblerVisitor(VariableStackAssigner stacker) {
        this.stacker = stacker;
    }

    @Override
    public String on(IRUnaryOpExpression ir){  // if argExpr uses only %r10, so does this
        String op = ir.getOperator();
        IRExpression argExpr = ir.getArgument();
        String code = argExpr.accept(this); // value in %r10
        if (op.equals("!")){
           code += "not %r10\n";
        }
        else { // "-"
           code += "neg %r10\n";
        }
        return code;
    }

    @Override
    public String on(IRBinaryOpExpression ir){
        return null; // TODO
    }

    @Override
    public String on(IRTernaryOpExpression ir){
        throw new RuntimeException("Ternaries should have been deconstructed into IF/THEN/ELSE before making assembly.");
    }

    @Override
    public String on(IRLenExpression ir){
        String arg = ir.getArgument();
        return "mov " + stacker.getMaxSize(arg) + ", %r10\n";
    }

    @Override
    public String on(IRVariableExpression ir){ // uses only %r10 unlesss array
        // TODO this doesn't handle global variables; maybe make it so stacker getAddress handles those?
        String code = "";
        if (ir.isArray()) {
            code += ir.getIndexExpression().accept(this);
            code += "mov " + stacker.getMaxSize(ir.getName()) + ", %r11\n";
            code += "cmp %r11, %r10\n";
            code += "jge .out_of_bounds\n";
            code += "cmp $0, %r10\n";
            code += "jl .out_of_bounds\n";
        }
        code += "mov " + stacker.getAddress(ir.getName()) + ", %r10\n";
        return code;
    }

    @Override
    public String on(IRMethodCallExpression ir){
        return null; // TODO
    }

    @Override
    public <T> String on(IRLiteral<T> ir){
        return null; // TODO
    }
}

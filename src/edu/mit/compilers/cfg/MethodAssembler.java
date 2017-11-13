package edu.mit.compilers.cfg;

import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.ir.expression.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class MethodAssembler implements CFGLine.CFGVisitor<String> {

    private String label;
    private int numParams;
    private VariableStackAssigner stacker;
    private TypeDescriptor returnType;

    public MethodAssembler(String method, int numParams, VariableStackAssigner stacker, TypeDescriptor returnType) {
        this.label = method;
        this.numParams = numParams;
        this.stacker = stacker;
        this.returnType = returnType;
    }

    public String assemble(CFG cfg) {
        return cfg.getStart().accept(this);
    }

    // NOTE: GUARANTEED TO ONLY USE %r10
    private String onDepthZeroExpression(IRExpression expr) {
        // TODO Auto-generated method stub
        return null;
    }

    private String onExpression(IRExpression expr) {
        // TODO Auto-generated method stub
        return null;
    }

    // compares the thing currently in %r10 to the var
    private String boundsCheck() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String on(CFGAssignStatement line) {
        String code = "";
        IRVariableExpression varAssigned = line.getVarAssigned();
        String stackLocation = stacker.getAddress(varAssigned.getName());  // includes %r10 as offset if array
        code += onExpression(line.getExpression());  // value now in %r10
        code += "push %r10\n"; // will get it out right before the end and assign to %r11

        if (varAssigned.isArray()) {
            code += onExpression(varAssigned.getIndexExpression()); // array index now in %r10
            // bounds checking
            code += "mov " + stacker.getMaxSize(varAssigned.getName()) + ", %r11\n";
            code += "cmp %r11, %r10\n";
            code += "jge .out_of_bounds\n";
            code += "cmp $0, %r10\n";
            code += "jl .out_of_bounds\n";

            // TODO whatever Maya was doing with global things in getCodeForIndexExpr in BlockAssembler
        }

        code += "pop %r11\n"; // value now in %r11
        code += "mov %r11, " + stackLocation + "\n";
        return code;
    }

    @Override
    public String on(CFGConditional line) {
        return onDepthZeroExpression(line.getExpression());
    }

    @Override
    public String on(CFGNoOp line) {
        return "";
    }

    @Override
    public String on(CFGReturn line) {
        String code = "";
        if (!line.isVoid()) {
            IRExpression returnExpr = line.getExpression();
            code += onDepthZeroExpression(returnExpr);  // return value now in %r10
            code += "mov %r10, %rax\n";
        }
        code += "jmp " + label + "_end\n"; // jump to end of method where we return
        return code;
    }

    @Override
    public String on(CFGMethodCall line) {
        String code = "";
        IRMethodCallExpression methodCall = line.getExpression();
        List<IRExpression> arguments = methodCall.getArguments();
        List<String> registers = new ArrayList<>(Arrays.asList("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"));
        for (int i=arguments.size()-1; i>=6; i--) {
            // if so many params we need stack pushes: iterate from size-1 down to 6 and push/pop them
            IRExpression arg = arguments.get(i);
            code += onDepthZeroExpression(arg);
            code += "push %r10\n";
        }
        for (int i=0; i<arguments.size() && i < 6; i++) {
            IRExpression arg = arguments.get(i);
            code += onDepthZeroExpression(arg);
            code += "mov %r10, " + registers.get(i) + "\n";
        }
        code += "mov $0, %rax\n";
        code += "call " + methodCall.getName() + "\n";
        for (int i=arguments.size()-1; i>=6; i--) {
            code += "pop %r10\n";
        }
        code += "mov %rax, %r10\n";
        return code;
    }

    @Override
    public String on(CFGBlock block) {
        // TODO Auto-generated method stub
        return null;
    }


}

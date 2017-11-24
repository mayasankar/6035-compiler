package edu.mit.compilers.cfg;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.math.BigInteger;
import java.util.Arrays;

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

    private String methodLabel;
    private VariableStackAssigner stacker;
    private Map<String, String> stringLabels;
    private int stringCount;

    public ExpressionAssemblerVisitor(String methodLabel, VariableStackAssigner stacker) {
        this.methodLabel = methodLabel;
        this.stacker = stacker;
        this.stringLabels = new HashMap<>();
        this.stringCount = 0;
    }

    public Map<String, String> getStringLabels() { return stringLabels; }

    @Override
    public String on(IRUnaryOpExpression ir){  // if argExpr uses only %r10, so does this
        String op = ir.getOperator();
        IRExpression argExpr = ir.getArgument();
        String code = argExpr.accept(this); // value in %r10
        if (op.equals("!")){
            code += "mov $1, %r11\n";
			code += "sub %r10, %r11\n";
			code += "mov %r11, %r10\n";
        }
        else { // "-"
           code += "neg %r10\n";
        }
        return code;
    }

    @Override
    public String on(IRTernaryOpExpression ir){
        throw new RuntimeException("Ternaries should have been deconstructed into IF/THEN/ELSE before making assembly.");
    }

    @Override
    public String on(IRLenExpression ir){ // uses only %r10
        String arg = ir.getArgument();
        return "mov " + stacker.getMaxSize(arg) + ", %r10\n";
    }

    @Override
    public String on(IRVariableExpression ir){ // uses only %r10 unlesss array
        String code = "";
        if (ir.isArray()) {
            code += ir.getIndexExpression().accept(this);
            code += "mov " + stacker.getMaxSize(ir.getName()) + ", %r11\n";
            code += "cmp %r11, %r10\n";
            code += "jge .out_of_bounds\n";
            code += "cmp $0, %r10\n";
            code += "jl .out_of_bounds\n";
        }
        code += stacker.moveTo(ir.getName(), "%r10", "%r10");
        return code;
    }

    @Override
    public String on(IRMethodCallExpression ir){  // uses only %r10 iff its argument expressions do
        String code = "";
        List<IRExpression> arguments = ir.getArguments();
        List<String> registers = new ArrayList<>(Arrays.asList("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"));
        for (int i=arguments.size()-1; i>=6; i--) {
            // if so many params we need stack pushes: iterate from size-1 down to 6 and push/pop them
            IRExpression arg = arguments.get(i);
            code += arg.accept(this);
            code += "push %r10\n";
        }
        for (int i=0; i<arguments.size() && i < 6; i++) {
            IRExpression arg = arguments.get(i);
            code += arg.accept(this);
            code += "mov %r10, " + registers.get(i) + "\n";
        }
        code += "mov $0, %rax\n";
        code += "call " + ir.getName() + "\n";
        for (int i=arguments.size()-1; i>=6; i--) {
            code += "pop %r10\n";
        }
        code += "mov %rax, %r10\n";
        return code;
    }

    @Override
    public String on(IRIntLiteral ir){  // uses %r10 only
        String valueAsStr = ir.toString();
        return "mov $" + valueAsStr + ", %r10\n";
    }

    @Override
    public String on(IRBoolLiteral ir){  // uses %r10 only
        Boolean booleanValue = ir.getValue();
        return (booleanValue ? "mov $1, %r10\n" : "mov $0, %r10\n");
    }

    @Override
    public String on(IRStringLiteral ir){  // uses %r10 only
        String stringValue = ir.toString();
        String label;
        if (! stringLabels.containsKey(stringValue)) {
            stringCount += 1;
            label = "."+methodLabel+"_string_"+new Integer(stringCount).toString();
            stringLabels.put(stringValue, label);
        }
        else {
            label = stringLabels.get(stringValue);
        }
        return "mov $" + label + ", %r10\n";
    }

    @Override
    public String on(IRBinaryOpExpression ir){  // definitely uses more than %r10
        String op = ir.getOperator();
        IRExpression leftExpr = ir.getLeftExpr();
        IRExpression rightExpr = ir.getRightExpr();
        String code = "";
        code += rightExpr.accept(this); // right value in %r10
        code += "mov %r10, %r11\n"; // right value in %r11
        code += leftExpr.accept(this); // left value in %r10, right value in %r11  (doesn't overwrite %r11 b/c subexprs required to be depth 0)
        switch (op) {
            case "+":
                code += "add %r11, %r10\n";  // expression output value in %r10
                return code;
            case "-":
                code += "sub %r11, %r10\n";
                return code;
            case "*":
                code += "imul %r11, %r10\n";
                return code;
            case "/": case "%":
                code += "mov $0, %rdx\n";
                code += "mov %r10, %rax\n";
                code += "cqto\n";
                code += "idiv %r11\n";
                code += "mov " + ((op.equals("/")) ? "%rax" : "%rdx") + ", %r10\n";
                return code;
            case "&&":
                code += "and %r11, %r10\n";
                return code;
            case "||":
                code += "or %r11, %r10\n";
                return code;
            case "==": case "!=": case "<": case ">": case "<=": case ">=":
                code += "cmp %r11, %r10\n";
                code += "mov $0, %r10\n";
                code += "mov $1, %r11\n";
                String dir = (op.equals("==")) ? "e" : (op.equals("!=")) ? "ne" : (op.equals("<")) ? "l" : (op.equals("<=")) ? "le" : (op.equals(">")) ? "g" : "ge";
                code += "cmov" + dir + " %r11, %r10\n";  // cmove, cmovne, cmovl, cmovg, cmovle, cmovge
                return code;
            default:
                throw new RuntimeException("unsupported operation in binary expression");
        }

    }
}

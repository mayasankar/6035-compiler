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
    public String on(IRUnaryOpExpression ir){  // uses %r11
        String op = ir.getOperator();
        IRExpression argExpr = ir.getArgument();
        String code = argExpr.accept(this); // value in %r10
        String register = "%r10";
        if (op.equals("!")){
            code += "mov $1, %r11\n";
			code += "sub " + register + ", %r11\n";
			code += "mov %r11, " + register + "\n";
        }
        else { // "-"
           code += "neg " + register + "\n";
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
        String register = "%r10";
        return "mov " + stacker.getMaxSize(arg) + ", " + register + "\n";
    }

    @Override
    public String on(IRVariableExpression ir){ // uses only %r10 unlesss array
        String code = "";
        String register = "%r10";
        if (ir.isArray()) {
            code += ir.getIndexExpression().accept(this); // TODO put result in register
            // code += "mov " + stacker.getMaxSize(ir.getName()) + ", %r11\n"; // TODO do we need this at all now we have CFGBoundsCheck?
            // code += "cmp %r11, " + register + "\n";
            // code += "jge .out_of_bounds\n";
            // code += "cmp $0, " + register + "\n";
            // code += "jl .out_of_bounds\n";
        }
        code += stacker.moveTo(ir.getName(), register, register);
        return code;
    }

    @Override
    public String on(IRMethodCallExpression ir){  // uses only %r10 iff its argument expressions do
        String code = "";
        String register = "%r10";
        List<IRExpression> arguments = ir.getArguments();
        List<String> registers = new ArrayList<>(Arrays.asList("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"));
        for (int i=arguments.size()-1; i>=6; i--) {
            // if so many params we need stack pushes: iterate from size-1 down to 6 and push/pop them
            IRExpression arg = arguments.get(i);
            code += arg.accept(this);
            code += "push " + register + "\n";
        }
        for (int i=0; i<arguments.size() && i < 6; i++) {
            IRExpression arg = arguments.get(i);
            code += arg.accept(this);
            code += "mov " + register + ", " + registers.get(i) + "\n";
        }
        code += "mov $0, %rax\n";
        code += "call " + ir.getName() + "\n";
        for (int i=arguments.size()-1; i>=6; i--) {
            code += "pop " + register + "\n";
        }
        code += "mov %rax, " + register + "\n";
        return code;
    }

    @Override
    public String on(IRIntLiteral ir){  // uses %r10 only
        String valueAsStr = ir.toString();
        String register = "%r10";
        return "mov $" + valueAsStr + ", " + register + "\n";
    }

    @Override
    public String on(IRBoolLiteral ir){  // uses %r10 only
        Boolean booleanValue = ir.getValue();
        String register = "%r10";
        return (booleanValue ? "mov $1, " + register + "\n" : "mov $0, " + register + "\n");
    }

    @Override
    public String on(IRStringLiteral ir){  // uses %r10 only
        String stringValue = ir.toString();
        String register = "%r10";
        String label;
        if (! stringLabels.containsKey(stringValue)) {
            stringCount += 1;
            label = "."+methodLabel+"_string_"+new Integer(stringCount).toString();
            stringLabels.put(stringValue, label);
        }
        else {
            label = stringLabels.get(stringValue);
        }
        return "mov $" + label + ", " + register + "\n";
    }

    @Override
    public String on(IRBinaryOpExpression ir){  // definitely uses %r11
        String register = "%r10";  // the register we want to output the expression into
        String leftReg = "%r10";  // the register the left expression should generate into
        String rightReg = "%r11";  // the register the right expression should generate into; distinct from leftReg
        String op = ir.getOperator();
        IRExpression leftExpr = ir.getLeftExpr();
        IRExpression rightExpr = ir.getRightExpr();
        String code = "";
        code += rightExpr.accept(this); // right value in %r10
        code += "mov %r10, " + rightReg + "\n"; // right value in %r11 // TODO once we can input regs, make it so right starts in rightReg
        code += leftExpr.accept(this); // left value in %r10, right value in %r11  (doesn't overwrite %r11 b/c subexprs required to be depth 0)
        switch (op) {
            case "+":
                code += "add " + rightReg + ", " + leftReg + "\n";
                code += "mov " + leftReg + ", " + register + "\n";
                return code;
            case "-":
                code += "sub " + rightReg + ", " + leftReg + "\n";
                code += "mov " + leftReg + ", " + register + "\n";
                return code;
            case "*":
                code += "imul " + rightReg + ", " + leftReg + "\n";
                code += "mov " + leftReg + ", " + register + "\n";
                return code;
            case "/": case "%":
                code += "mov $0, %rdx\n";
                code += "mov " + leftReg + ", %rax\n";
                code += "cqto\n";
                code += "idiv " + rightReg + "\n";
                code += "mov " + ((op.equals("/")) ? "%rax" : "%rdx") + ", " + register + "\n";
                return code;
            case ">>":
                code += "mov " + rightReg + ", %rcx\n";  // TODO this creates a problem of overwriting %rcx, how do we fix?
                code += "shr %cl," + leftReg + "\n";
                code += "mov " + leftReg + ", " + register + "\n";
                return code;
            case "<<":
                code += "mov " + rightReg + ", %rcx\n";  // TODO this creates a problem of overwriting %rcx, how do we fix?
                code += "shl %cl, " + leftReg + "\n";
                code += "mov " + leftReg + ", " + register + "\n";
                return code;
            case "&&":
                code += "and " + rightReg + ", " + leftReg + "\n";
                code += "mov " + leftReg + ", " + register + "\n";
                return code;
            case "||":
                code += "or " + rightReg + ", " + leftReg + "\n";
                code += "mov " + leftReg + ", " + register + "\n";
                return code;
            case "==": case "!=": case "<": case ">": case "<=": case ">=":
                code += "cmp " + rightReg + ", " + leftReg + "\n";
                code += "mov $0, " + register + "\n";
                code += "mov $1, %r11\n";
                String dir = (op.equals("==")) ? "e" : (op.equals("!=")) ? "ne" : (op.equals("<")) ? "l" : (op.equals("<=")) ? "le" : (op.equals(">")) ? "g" : "ge";
                code += "cmov" + dir + " %r11, " + register + "\n";  // cmove, cmovne, cmovl, cmovg, cmovle, cmovge
                return code;
            default:
                throw new RuntimeException("unsupported operation in binary expression");
        }

    }
}

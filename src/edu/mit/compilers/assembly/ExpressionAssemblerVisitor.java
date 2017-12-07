package edu.mit.compilers.assembly;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.math.BigInteger;
import java.util.Arrays;

import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.assembly.lines.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;
import edu.mit.compilers.cfg.lines.*;

public class ExpressionAssemblerVisitor implements IRExpression.IRExpressionVisitor<List<AssemblyLine>> {

    private String methodLabel;
    private CFGLocationAssigner stacker;
    private Map<String, String> stringLabels;
    private int stringCount;
    
    private String freeRegister;
    private CFGLine cfgline;

    public ExpressionAssemblerVisitor(String methodLabel, CFGLocationAssigner stacker) {
        this.methodLabel = methodLabel;
        this.stacker = stacker;
        this.stringLabels = new HashMap<>();
        this.stringCount = 0;
    }

    public Map<String, String> getStringLabels() { return stringLabels; }

    public List<AssemblyLine> onCFGAssignExpr(CFGAssignStatement line) {
        IRVariableExpression varAssigned = line.getVarAssigned();
        freeRegister = stacker.getFreeRegister(line);
        cfgline = line;
        List<AssemblyLine> lines = line.getExpression().accept(this);  // value now in freeRegister
        String storeLoc = stacker.getLocationOfVariable(line.getVarAssigned(), line);
        if (varAssigned.isArray()){
            lines.add(new APush(freeRegister)); // will get it out right before the end and assign to %r11
            lines.addAll(varAssigned.getIndexExpression().accept(this)); // array index now in freeRegister
            lines.add(new APop("%r11"));
        }
        else {
            lines.add(new AMov("%r10", "%r11"));
        }
        lines.addAll(stacker.moveFrom(varAssigned.getName(), "%r11", "%r10"));
        return lines;
    }
    
    @Override
    public List<AssemblyLine> on(IRUnaryOpExpression ir){
        String op = ir.getOperator();
        IRExpression argExpr = ir.getArgument();
        List<AssemblyLine> lines = argExpr.accept(this); // value in free register
        if (op.equals("!")){
            lines.add(new AOps("xorq", "$1", freeRegister));
        }
        else { // "-"
            lines.add(new AUnaryOp("neg", freeRegister));
        }
        return lines;
    }

    @Override
    public List<AssemblyLine> on(IRTernaryOpExpression ir){
        throw new RuntimeException("Ternaries should have been deconstructed into IF/THEN/ELSE before making assembly.");
    }

    @Override
    public List<AssemblyLine> on(IRLenExpression ir){ 
        String arg = ir.getArgument();
        List<AssemblyLine> lines = new ArrayList<>();
        lines.add(new AMov(stacker.getMaxSize(arg), freeRegister));
        return lines;
    }

    @Override
    public List<AssemblyLine> on(IRVariableExpression ir){ // uses only %r10 unlesss array
        List<AssemblyLine> lines = new ArrayList<>();
        if (ir.isArray()) {
            lines.addAll(ir.getIndexExpression().accept(this)); // puts index into freeRegister
        }
        lines.addAll(stacker.pullFromStack(ir.getName(), freeRegister, freeRegister));
        return lines;
    }

    @Override
    public List<AssemblyLine> on(IRMethodCallExpression ir){  // uses only %r10 iff its argument expressions do
        List<AssemblyLine> lines = new ArrayList<>();
        IRMethodCallExpression methodCall = line.getExpression();
        List<IRExpression> arguments = methodCall.getArguments();
        String[] registers = {"%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"};
        List<String> callerSavedRegisters = new LinkedList<>();
        
        // Move first six arguments to the correct register, as by convention
        for (int i=0; i<arguments.size() && i < 6; i++) {
            String reg = registers[i];
            // If the register is currently in use, pop the value to the stack to remember it
            if(!stacker.isFreeRegister(reg, line)) {
                lines.add(new APush(reg));
                callerSavedRegisters.add(0, reg);
            }
            IRExpression arg = arguments.get(i);
            String argLoc = stacker.getLocationOfVariable(arg, line);
            lines.add(new AMov(argLoc, reg));
        }
        if(!stacker.isFreeRegister("%rax", line)) {
            lines.add(new APush("%rax"));
            callerSavedRegisters.add(0, "%rax");
        }
        lines.add(new AMov("$0", "%rax"));
        
        // Move remaining args onto stack
        for (int i=arguments.size()-1; i>=6; i--) {
            // if so many params we need stack pushes: iterate from size-1 down to 6 and push/pop them
            IRExpression arg = arguments.get(i);
            String argLoc = stacker.getLocationOfVariable(arg, line);
            if(stacker.isStoredInRegister(arg, line)) {
                lines.add(new APush(argLoc));
            } else if (callerSavedRegisters.contains(argLoc)){ // if an arg has been moved to make space for another one
                String stackLoc = 8 * (callerSavedRegisters.indexOf(argLoc) + 1) + "(%rsp)"; // TODO check for off by one errors here
                lines.add(new AMov(stackLoc, freeRegister));
                lines.add(new APush(freeRegister));
            } else {
                lines.add(new AMov(argLoc, freeRegister));
                lines.add(new APush(freeRegister));
            }
        }

        lines.add(new ACall(methodCall.getName()));
        for (int i=arguments.size()-1; i>=6; i--) {// TODO: Just decrease the stack pointer
            lines.add(new APop(freeRegister));
        }
        lines.add(new AMov("%rax", freeRegister));
        return lines;
        
    }

    @Override
    public List<AssemblyLine> on(IRIntLiteral ir){  // uses %r10 only
        String valueAsStr = "$" + ir.toString();
        String register = "%r10";
        List<AssemblyLine> lines = new ArrayList<>();
        lines.add(new AMov(valueAsStr, register));
        return lines;
    }

    @Override
    public List<AssemblyLine> on(IRBoolLiteral ir){  // uses %r10 only
        Boolean booleanValue = ir.getValue();
        List<AssemblyLine> lines = new ArrayList<>();
        lines.add(new AMov(booleanValue ? "$1" : "$0", freeRegister));
        return lines;
    }

    @Override
    public List<AssemblyLine> on(IRStringLiteral ir){  // uses %r10 only
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
        List<AssemblyLine> lines = new ArrayList<>();
        lines.add(new AMov("$" + label, register));
        return lines;
    }

    @Override
    public List<AssemblyLine> on(IRBinaryOpExpression ir){  // definitely uses %r11
        String register = "%r10";  // the register we want to output the expression into
        String leftReg = "%r10";  // the register the left expression should generate into
        String rightReg = "%r11";  // the register the right expression should generate into; distinct from leftReg
        String op = ir.getOperator();
        IRExpression leftExpr = ir.getLeftExpr();
        IRExpression rightExpr = ir.getRightExpr();

        List<AssemblyLine> lines = new ArrayList<>();
        lines.addAll(rightExpr.accept(this)); // right value in %r10
        lines.add(new AMov("%r10", rightReg)); // right value in %r11 // TODO once we can input regs, make it so right starts in rightReg
        lines.addAll(leftExpr.accept(this)); // left value in %r10, right value in %r11  (doesn't overwrite %r11 b/c subexprs required to be depth 0)
        switch (op) {
            case "+":
                lines.add(new AOps("add", rightReg, leftReg));
                lines.add(new AMov(leftReg, register));
                return lines;
            case "-":
                lines.add(new AOps("sub", rightReg, leftReg));
                lines.add(new AMov(leftReg, register));
                return lines;
            case "*":
                lines.add(new AOps("imul", rightReg, leftReg));
                lines.add(new AMov(leftReg, register));
                return lines;
            case "/": case "%":
                lines.add(new AMov("$0", "%rdx"));
                lines.add(new AMov(leftReg, "%rax"));
                lines.add(new ACommand("cqto"));
                lines.add(new AUnaryOp("idiv", rightReg));
                lines.add(new AMov((op.equals("/")) ? "%rax" : "%rdx", register));
                return lines;
            case ">>":
            case "<<":
                lines.add(new AMov(rightReg, "%rcx"));  // NOTE this creates a problem of overwriting %rcx, how do we fix?
                lines.add(new AShift((op.equals(">>")) ? "shr" : "shl", register));
                lines.add(new AMov(leftReg, register));
                return lines;
            case "&&":
                lines.add(new AOps("and", rightReg, leftReg));
                lines.add(new AMov(leftReg, register));
                return lines;
            case "||":
                lines.add(new AOps("or", rightReg, leftReg));
                lines.add(new AMov(leftReg, register));
                return lines;
            case "==": case "!=": case "<": case ">": case "<=": case ">=":
                lines.add(new ACmp(rightReg, leftReg));
                lines.add(new AMov("$0", register));
                lines.add(new AMov("$1", "%r11"));
                String dir = (op.equals("==")) ? "e" : (op.equals("!=")) ? "ne" : (op.equals("<")) ? "l" : (op.equals("<=")) ? "le" : (op.equals(">")) ? "g" : "ge";
                lines.add(new ACmov("cmov" + dir, "%r11", register));  // cmove, cmovne, cmovl, cmovg, cmovle, cmovge
                return lines;
            default:
                throw new RuntimeException("unsupported operation in binary expression");
        }

    }
}

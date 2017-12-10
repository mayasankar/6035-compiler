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

    private String exprName = null;
    
    public String freeRegister;
    public CFGMethodCall method;
    private CFGAssignStatement line;

    public ExpressionAssemblerVisitor(String methodLabel, CFGLocationAssigner stacker) {
        this.methodLabel = methodLabel;
        this.stacker = stacker;
        this.stringLabels = new HashMap<>();
        this.stringCount = 0;
    }

    public Map<String, String> getStringLabels() { return stringLabels; }

    public String getExprName(IRExpression expr) {
        expr.accept(this); // makes sure strings are kept track of
        if(exprName == null) {
            throw new RuntimeException("Do not call on things which are not depth 0.");
        }
        String ans = exprName;
        exprName = null;
        return ans;
        
    }
    
    public List<AssemblyLine> onCFGAssignExpr(CFGAssignStatement line) {
        IRVariableExpression varAssigned = line.getVarAssigned();
        String sourceReg = stacker.getFreeRegister(line);
        String indexReg = stacker.getIndexRegister(line);
        freeRegister = sourceReg;
        List<AssemblyLine> lines = line.getExpression().accept(this);  // value now in freeRegister
        String storeName = getExprName(varAssigned);
        if (varAssigned.isArray()){
            freeRegister = indexReg;
            lines.addAll(varAssigned.getIndexExpression().accept(this)); // array index now in indexReg
            
        }
        lines.addAll(stacker.moveToStore(storeName, sourceReg, indexReg));

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
        exprName = ir.getName();
        List<AssemblyLine> lines = new ArrayList<>();
        if (ir.isArray()) {
            lines.addAll(ir.getIndexExpression().accept(this)); // puts index into freeRegister
        }
        lines.addAll(stacker.moveFromStore(ir.getName(), freeRegister, freeRegister));
        return lines;
    }

    @Override
    public List<AssemblyLine> on(IRMethodCallExpression ir){  // must set cfgline
        List<AssemblyLine> lines = new ArrayList<>();
        CFGMethodCall line = method;
        String freeRegister = stacker.getFreeRegister(method);
        IRMethodCallExpression methodCall = ir;
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
            String argName = getExprName(arg);
            String argLoc = stacker.getLocationOfVariable(argName, line);
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
            String argName = getExprName(arg);
            String argLoc = stacker.getLocationOfVariable(argName, line);
            if(stacker.isVarStoredInRegister(argName, line)) {
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
        exprName = valueAsStr;
        List<AssemblyLine> lines = new ArrayList<>();
        lines.add(new AMov(valueAsStr, freeRegister));
        return lines;
    }

    @Override
    public List<AssemblyLine> on(IRBoolLiteral ir){  // uses %r10 only
        Boolean booleanValue = ir.getValue();
        String convertedValue = booleanValue ? "$1" : "$0";
        exprName = convertedValue;
        List<AssemblyLine> lines = new ArrayList<>();
        lines.add(new AMov(convertedValue, freeRegister));
        return lines;
    }

    @Override
    public List<AssemblyLine> on(IRStringLiteral ir){  // uses %r10 only
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
        exprName = "$" + label;
        List<AssemblyLine> lines = new ArrayList<>();
        lines.add(new AMov("$" + label, freeRegister));
        return lines;
    }

    @Override
    public List<AssemblyLine> on(IRBinaryOpExpression ir){  // definitely uses %r11
        String op = ir.getOperator();
        IRExpression leftExpr = ir.getLeftExpr();
        IRExpression rightExpr = ir.getRightExpr();
        String rightName = getExprName(rightExpr);
        String rExprLoc = stacker.getLocationOfVariable(rightName, line);

        List<AssemblyLine> lines = new ArrayList<>();
        lines.addAll(leftExpr.accept(this)); // left value in freeRegister
        switch (op) {
            case "+":
                lines.add(new AOps("add", rExprLoc, freeRegister));
                return lines;
            case "-":
                lines.add(new AOps("sub", rExprLoc, freeRegister));
                return lines;
            case "*":
                lines.add(new AOps("imul", rExprLoc, freeRegister));
                return lines;
            case "/": case "%":
                lines.add(new APush("%rax"));
                lines.add(new APush("%rdx"));
                lines.add(new AMov("$0", "%rdx"));
                lines.add(new AMov(freeRegister, "%rax"));
                lines.add(new ACommand("cqto"));
                lines.addAll(rightExpr.accept(this)); // Right expr now in freeRegister
                lines.add(new AUnaryOp("idiv", freeRegister));
                lines.add(new AMov((op.equals("/")) ? "%rax" : "%rdx", freeRegister));
                lines.add(new APop("%rdx"));
                lines.add(new APop("%rax"));
                return lines;
            case ">>":
            case "<<":
                lines.add(new APush("%rcx"));
                String freeRegCopy = freeRegister;
                freeRegister = "%rcx";
                lines.addAll(rightExpr.accept(this));// right expr now in %rcx
                freeRegister = freeRegCopy;
                lines.add(new AShift((op.equals(">>")) ? "shr" : "shl", freeRegister));
                lines.add(new APop("%rcx"));
                return lines;
            case "&&":
                lines.add(new AOps("and", rExprLoc, freeRegister));
                return lines;
            case "||":
                lines.add(new AOps("or", rExprLoc, freeRegister));
                return lines;
            case "==": case "!=": case "<": case ">": case "<=": case ">=":
                lines.add(new ACmp(rExprLoc, freeRegister));
                lines.add(new AMov("$0", freeRegister));
                String secondReg = (freeRegister.equals("%r10"))? "%r11": "%r10";
                lines.add(new APush(secondReg));
                lines.add(new AMov("$1", secondReg));
                String dir = (op.equals("==")) ? "e" : (op.equals("!=")) ? "ne" : (op.equals("<")) ? "l" : (op.equals("<=")) ? "le" : (op.equals(">")) ? "g" : "ge";
                lines.add(new ACmov("cmov" + dir, secondReg, freeRegister));  // cmove, cmovne, cmovl, cmovg, cmovle, cmovge
                lines.add(new APop(secondReg));
                return lines;
            default:
                throw new RuntimeException("unsupported operation in binary expression");
        }

    }
}
    

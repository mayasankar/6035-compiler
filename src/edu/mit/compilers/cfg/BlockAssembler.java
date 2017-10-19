package edu.mit.compilers.cfg;

import java.io.OutputStream;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.operator.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;


public class BlockAssembler {

    String methodLabel;
    int blockCount;
    int numAllocs;
    Map<CFGBlock, String> blockLabels;

    public BlockAssembler(String label, int numParams) {
        this.methodLabel = label;
        this.blockCount = 0;
        this.numAllocs = numParams;  // will increment this as we add locals
        this.blockLabels = new HashMap<>();
    }

    public String makeCode(CFGBlock block) {
        String prefix = methodLabel + ":\n";
        String code = "";
        // TODO mov input params to stack

        code += makeCodeHelper(block);

        String allocSpace = new Integer(8*numAllocs).toString();
        code += "\n"+ methodLabel + "_end:\n";
        code += "leave\n" + "ret\n";
        prefix += "enter $" + allocSpace + ", $0\n";
        return prefix + code;
    }

    private String makeCodeHelper(CFGBlock block) {
        if (blockLabels.containsKey(block)) {
            throw new RuntimeException("Making code for a block that already has code generated.");
        }
        blockCount += 1;
        String label = methodLabel+"_"+new Integer(blockCount).toString();
        blockLabels.put(block, label);
        String code = "\n" + label + ":\n";
        for (CFGLine line : block.getLines()) {
            code += makeCodeLine(line);
        }

        // add code for true child
        String childCode = "";
        CFGBlock child = (CFGBlock)block.getTrueBranch();
        if (child != null) {
            if (!blockLabels.containsKey(child)) {
                childCode += makeCodeHelper(child);
            }
            else if (block.isBranch()) {
                code += "mov $1, %r11\n";
                code += "cmp %r10, %r11\n";
                code += "je " + blockLabels.get(child) + "\n";
            }
            else {
                code += "jmp " + blockLabels.get(child) + "\n";
            }
        }
        else {
            //null child means we want to jump to the end of method where we return
            code += "jmp " + methodLabel + "_end\n";
        }
        if (block.isBranch()) {
            //  add code for false child, and jump statement
            child = (CFGBlock)block.getFalseBranch();
            if (child != null) {
                if (!blockLabels.containsKey(child)) {
                    childCode += makeCodeHelper(child);
                }
                // jump to it
                code += "mov $0, %r11\n";
                code += "cmp %r10, %r11\n";
                code += "je " + blockLabels.get(child) + "\n";
            }
            else {
                throw new RuntimeException("null false child of block when true child is not null.");
            }
        }
        return code + childCode;
    }

    private String makeCodeLine(CFGLine line) {
        /*
        CFGExpression: compute result of expression and store in %r10 so it can be used to jump/etc. next
        CFGStatement: compute result and store in given variable
        CFGDecl: increment numAllocs???
        CFGBlock: RuntimeException
        CFGNoOp: empty code
        CFGMethodDecl: call methodName???
        */
        String code = "";

        try {
            if (line instanceof CFGNoOp) {
                code += "";
            }
            else if (line instanceof CFGDecl) {
                code += makeCodeCFGDecl((CFGDecl)line);
            }
            else if (line instanceof CFGExpression) {
                code += makeCodeCFGExpression((CFGExpression)line);
            }
            else if (line instanceof CFGMethodDecl) {
                code += makeCodeCFGMethodDecl((CFGMethodDecl)line);
            }
            else if (line instanceof CFGStatement) {
                code += makeCodeCFGStatement((CFGStatement)line);
            }
            else {
                throw new RuntimeException("CFGLine of unaccepted type.");
            }
        }
        catch (RuntimeException e) {
            code = line.ownValue() + "\n";
        }
        return code;
        //return line.ownValue() + "\n";
        //throw new RuntimeException("Unimplemented");
    }

    private String makeCodeCFGDecl(CFGDecl line) {
        throw new RuntimeException("Unimplemented");
    }

    private String makeCodeCFGExpression(CFGExpression line) {
        throw new RuntimeException("Unimplemented");
    }

    private String makeCodeCFGMethodDecl(CFGMethodDecl line) {
        throw new RuntimeException("Unimplemented");
    }

    private String makeCodeCFGStatement(CFGStatement line) {
        IRStatement statement = line.getStatement();
        String code = "";
        switch(statement.getStatementType()) {
            case IF_BLOCK: case FOR_BLOCK: case WHILE_BLOCK: {
                throw new RuntimeException("if/for/while statements should have been destructed.");
            }
            case METHOD_CALL: {
                IRMethodCallExpression methodCall = ((IRMethodCallStatement)statement).getMethodCall();
                List<IRExpression> arguments = methodCall.getArguments();
                for (int i=arguments.size()-1; i>=0; i--) {
                    IRExpression arg = arguments.get(i);
                    code += makeCodeIRExpression(arg);
                    code += "push %r10\n";
                }
                code += "call " + methodCall.getName() + "\n";
                return code;
            }
            case RETURN_EXPR: {
                IRExpression returnExpr = ((IRReturnStatement) statement).getReturnExpr();
                code += makeCodeIRExpression(returnExpr);  // return value now in %r10
                code += "mov %r10, %rax\n";
                code += "leave\n";
                code += "ret\n";
                return code;
            }
            case ASSIGN_EXPR: {
                IRAssignStatement s = (IRAssignStatement)statement;
                IRVariableExpression varAssigned = s.getVarAssigned();
            	String operator = s.getOperator();
            	IRExpression value = s.getValue();
                if (value != null){
                    code += makeCodeIRExpression(value);  // value now in %r10
                }
                String stackLocation = getVariableStackLocation(varAssigned);
                switch (operator) {
                    case "=":
                        code += "mov %r10, " + stackLocation + "\n";
                        return code;
                    case "+=":
                        code += "mov " + stackLocation +", %r11\n";
                        code += "add %r10, %r11\n";
                        code += "mov %r11, " + stackLocation + "\n";
                        return code;
                    case "-=":
                        code += "mov " + stackLocation +", %r11\n";
                        code += "sub %r10, %r11\n";
                        code += "mov %r11, " + stackLocation + "\n";
                        return code;
                    case "++":
                        code += "mov " + stackLocation +", %r11\n";
                        code += "add $1, %r11\n";
                        code += "mov %r11, " + stackLocation + "\n";
                        return code;
                    case "--":
                        code += "mov " + stackLocation +", %r11\n";
                        code += "sub $1, %r11\n";
                        code += "mov %r11, " + stackLocation + "\n";
                        return code;
                }
            }
            case BREAK: {
                // TODO
            }
            case CONTINUE: {
                // TODO
            } default: {
                throw new RuntimeException("destructIR error: UNSPECIFIED statement");
            }
        }
    }

    // return the code to evaluate the expression and store its result in %r10
    private String makeCodeIRExpression(IRExpression expr) {
        //TODO
        return "<CODE FOR EXPRESSION " + expr.toString() + ">\n";
        //throw new RuntimeException("Unimplemented");
    }

    private String getVariableStackLocation(IRVariableExpression var) {
        // TODO
        return "-8(%rbp)";
        //throw new RuntimeException("Unimplemented");
    }
}

package edu.mit.compilers.assembly;

import java.util.Map;

import edu.mit.compilers.cfg.CFG;
import edu.mit.compilers.cfg.CFGBlock;
import edu.mit.compilers.cfg.CFGLocationAssigner;
import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.assembly.lines.*;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.decl.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

public class MethodAssembler implements CFGLine.CFGVisitor<List<AssemblyLine>> {

    private String label;
    private int numAllocs;
    private CFGLocationAssigner stacker;
    private TypeDescriptor returnType;
	private IRMethodDecl decl;

    private Map<CFGBlock, String> blockNames;
    private int blockCount;

    private ExpressionAssemblerVisitor expressionAssembler;


    public MethodAssembler(String method, int numParams, CFGLocationAssigner stacker, TypeDescriptor returnType, IRMethodDecl decl) {
        this.label = method;
        this.numAllocs = stacker.getNumAllocs();
        this.stacker = stacker;
        this.returnType = returnType;
        this.blockNames = new HashMap<>();
        this.blockCount = 0;
		this.decl = decl;
        this.expressionAssembler = new ExpressionAssemblerVisitor(label, stacker);
    }

    public List<AssemblyLine> assemble(CFG cfg) {
        List<AssemblyLine> prefixLines = new ArrayList<>();
        prefixLines.add(new ALabel(label));

        List<AssemblyLine> lines = stacker.pullInArguments(decl);

		lines.addAll(cfg.getStart().getCorrespondingBlock().accept(this));

        // if it doesn't have anywhere returning, but should, have it jump to the runtime error
        if (this.returnType != TypeDescriptor.VOID && !label.equals("main")) {
            lines.add(new AJmp("jmp", ".nonreturning_method"));
        }

        // if it has void return, or if a return statement tells it to jump here, leave
        lines.add(new AWhitespace());
        lines.add(new ALabel(label + "_end"));

        if (label.equals("main")) { // makes sure exit code is 0
            lines.add(new AMov("$0", "%rax"));
        }
        lines.add(new ACommand("leave"));
        lines.add(new ACommand("ret"));

        // String literals
        Map<String, String> stringLabels = expressionAssembler.getStringLabels();
        for (String stringValue : stringLabels.keySet()){
            String label = stringLabels.get(stringValue);
            lines.add(new ALabel(label));
            lines.add(new AString(stringValue));
        }

        // figure out how many allocations we did
        String allocSpace = new Integer(8*numAllocs).toString();
        prefixLines.add(new ACommand("enter $" + allocSpace + ", $0"));

        prefixLines.addAll(lines);
        return prefixLines;
    }
/*
	private List<AssemblyLine> pullInArguments() {
        List<AssemblyLine> lines = new ArrayList<>();
        List<IRMemberDecl> parameters = decl.getParameters().getVariableList();
     	for(int i=0; i < parameters.size(); ++i) {
     		IRMemberDecl param = parameters.get(i);
            String paramLoc = getParamLoc(i);

     		if (i<=5) {
                lines.addAll(stacker.moveFrom(param.getName(), paramLoc, "%r10"));
     		} else {
                lines.add(new AMov(paramLoc, "%r11"));
     			lines.addAll(stacker.moveFrom(param.getName(), "%r11", "%r10"));
     		}
     	}
     	return lines;
	}

    private String getParamLoc(int i) {
     	if(i==0) {
     		return "%rdi";
     	} else if (i==1) {
     		return "%rsi";
     	} else if (i==2) {
     		return "%rdx";
     	} else if (i==3) {
     		return "%rcx";
     	} else if (i==4) {
     		return "%r8";
     	} else if (i==5) {
     		return "%r9";
     	} else {
     		return (i-4)*8 + "(%rbp)";
     	}
     }
*/
    // NOTE: GUARANTEED TO ONLY USE %r10


    @Override
    public List<AssemblyLine> on(CFGAssignStatement line) {
        return expressionAssembler.onCFGAssignExpr(line);
    }

    @Override
    public List<AssemblyLine> on(CFGBoundsCheck line) {
        IRVariableExpression variable = line.getExpression();
        List<AssemblyLine> lines = new ArrayList<>();
        IRExpression index = variable.getIndexExpression();
        String indexRegister;
        if(!stacker.isExpressionStoredInRegister(index, line)) {
            indexRegister = stacker.getFreeRegister(line);
        } else {
            indexRegister = stacker.getLocationOfVarExpression(index, line);
        }
        lines.add(new ACmp("$0", indexRegister));
        lines.add(new AJmp("jl", ".out_of_bounds"));

        lines.add(new ACmp(stacker.getMaxSize(variable.getName()), indexRegister));
        lines.add(new AJmp("jle", ".out_of_bounds"));

        return lines;
    }

    @Override
    public List<AssemblyLine> on(CFGConditional line) {
        List<AssemblyLine> lines = new ArrayList<>();
        IRExpression branchExpr = line.getExpression();
        String branchLoc;
        if(stacker.isExpressionStoredInRegister(branchExpr, line)) {
            branchLoc = stacker.getLocationOfVarExpression(branchExpr, line);
        } else {
            branchLoc = stacker.getFreeRegister(line);
            lines.add(new AMov(stacker.getLocationOfVarExpression(branchExpr, line), branchLoc));
        }
        lines.add(new ACmp("$0", branchLoc));
        lines.add(new AJmp("je", blockNames.get(line.getCorrespondingBlock().getFalseBranch()))); // this line needs to go after the visitor on the falseBranch so the label has been generated

        return lines;
    }

    @Override
    public List<AssemblyLine> on(CFGNoOp line) {
        return new ArrayList<AssemblyLine>();
    }

    @Override
    public List<AssemblyLine> on(CFGNoReturnError line) {
        List<AssemblyLine> answer = new ArrayList<>();
        answer.add(new AJmp("jmp", ".nonreturning_method"));
        return answer;
    }

    @Override
    public List<AssemblyLine> on(CFGReturn line) {
        List<AssemblyLine> lines = new ArrayList<>();
        if (!line.isVoid()) {
            IRExpression returnExpr = line.getExpression();
            String answerLoc = stacker.getLocationOfVarExpression(returnExpr, line);
            lines.add(new AMov(answerLoc, "%rax"));
        }
        lines.add(new AJmp("jmp", label + "_end")); // jump to end of method where we return
        return lines;
    }

    @Override
    public List<AssemblyLine> on(CFGMethodCall line) {
        List<AssemblyLine> lines = new ArrayList<>();
        IRMethodCallExpression methodCall = line.getExpression();
        List<IRExpression> arguments = methodCall.getArguments();
        String[] registers = {"%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"};
        List<String> callerSavedRegisters = new LinkedList<>();
        String freeRegister = stacker.getFreeRegister(line);

        // Move first six arguments to the correct register, as by convention
        for (int i=0; i<arguments.size() && i < 6; i++) {
            String reg = registers[i];
            // If the register is currently in use, pop the value to the stack to remember it
            if(!stacker.isFreeRegister(reg, line)) {
                lines.add(new APush(reg));
                callerSavedRegisters.add(0, reg);
            }
            IRExpression arg = arguments.get(i);
            String argLoc = stacker.getLocationOfVarExpression(arg, line);
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
            String argLoc = stacker.getLocationOfVarExpression(arg, line);
            if(stacker.isExpressionStoredInRegister(arg, line)) {
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
        return lines;
    }

    @Override
    public List<AssemblyLine> on(CFGBlock block) {
        if (blockNames.containsKey(block)) {
            return new ArrayList<>();
        } else {
            blockNames.put(block, "." + label + "_" + blockCount);
            blockCount += 1;
        }
        boolean jumpToTrue = false;

        List<AssemblyLine> lines = new ArrayList<>();
        lines.add(new AWhitespace());
        lines.add(new ALabel(blockNames.get(block)));

        List<AssemblyLine> childrenLines = new ArrayList<>();
        if (! block.isEnd()) {
			jumpToTrue = blockNames.containsKey(block.getTrueBranch());
			childrenLines = block.getTrueBranch().accept(this);
            if (block.isBranch()) {
            	childrenLines.addAll(block.getFalseBranch().accept(this));
            }
        }
        for (CFGLine line: block.getLines()) {
            lines.addAll(line.accept(this));
        }

		if (jumpToTrue) {
            lines.add(new AJmp("jmp", blockNames.get(block.getTrueBranch())));
		}
		if (block.isEnd() && label.equals("main")) {
            lines.add(new AJmp("jmp", label + "_end"));
		}
        lines.addAll(childrenLines);
        return lines;
    }
}

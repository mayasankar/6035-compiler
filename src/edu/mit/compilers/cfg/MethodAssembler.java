package edu.mit.compilers.cfg;

import java.util.Map;

import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.decl.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class MethodAssembler implements CFGLine.CFGVisitor<String> {

    private String label;
    private int numAllocs;
    private VariableStackAssigner stacker;
    private TypeDescriptor returnType;
	private IRMethodDecl decl;

    private Map<CFGBlock, String> blockNames;
    private int blockCount;

    private ExpressionAssemblerVisitor expressionAssembler;


    public MethodAssembler(String method, int numParams, VariableStackAssigner stacker, TypeDescriptor returnType, IRMethodDecl decl) {
        this.label = method;
        this.numAllocs = stacker.getNumAllocs();
        this.stacker = stacker;
        this.returnType = returnType;
        this.blockNames = new HashMap<>();
        this.blockCount = 0;
		this.decl = decl;
        this.expressionAssembler = new ExpressionAssemblerVisitor(label, stacker);
    }

    public String assemble(CFG cfg) {
        String prefix = label + ":\n";
        String code = "";
		code += pullInArguments();
		code += cfg.getStart().getCorrespondingBlock().accept(this);

        // if it doesn't have anywhere returning, but should, have it jump to the runtime error
        if (this.returnType != TypeDescriptor.VOID) {
            code += "jmp .nonreturning_method\n";
        }

        // if it has void return, or if a return statement tells it to jump here, leave
        code += "\n"+ label + "_end:\n";
        if (label.equals("main")) { // makes sure exit code is 0
            code += "mov $0, %rax\n";
        }
        code += "leave\n" + "ret\n\n";

        // String literals
        Map<String, String> stringLabels = expressionAssembler.getStringLabels();
        for (String stringValue : stringLabels.keySet()){
            String label = stringLabels.get(stringValue);
            code += "\n" + label + ":\n";
            code += ".string " + stringValue + "\n";
        }

        // figure out how many allocations we did
        String allocSpace = new Integer(8*numAllocs).toString();  // TODO is this the right number? it's # variables in stacker
        prefix += "enter $" + allocSpace + ", $0\n";

        return prefix + code;
    }

	private String pullInArguments() {
        String code = "";
        List<IRMemberDecl> parameters = decl.getParameters().getVariableList();
     	for(int i=0; i < parameters.size(); ++i) {
     		IRMemberDecl param = parameters.get(i);
     		String paramStackLoc = stacker.getAddress(param.getName());
     		String paramLoc = getParamLoc(i);
     		if(i<=5) {
     			code += String.format("mov %s, %s\n", paramLoc, paramStackLoc);
     		} else {
     			code += String.format("mov %s, %%r10\n", paramLoc);
     			code += String.format("mov %%r10, %s\n", paramStackLoc);
     		}
     	}
     	return code;
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

    // NOTE: GUARANTEED TO ONLY USE %r10
    private String onDepthZeroExpression(IRExpression expr) {
        // TODO Arkadiy did you want to refactor this?
        if (expr.getDepth() > 0) {
            throw new RuntimeException("Called onDepthZeroExpression on expression of non-zero depth.");
        }
        return expr.accept(expressionAssembler);
    }

    private String onExpression(IRExpression expr) {
        // TODO Arkadiy did you want to refactor this?
        return expr.accept(expressionAssembler);
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
        if (blockNames.containsKey(block)) {
            return "";
        } else {
            blockNames.put(block, "." + label + "_" + blockCount);
            blockCount += 1;
        }
        boolean jumpToTrue = false;
        String code = "\n" + blockNames.get(block) + ":\n";
        String childrenCode = "";
        if (! block.isEnd()) {
			jumpToTrue = blockNames.containsKey(block.getTrueBranch());
			childrenCode = block.getTrueBranch().accept(this);
            if(block.isBranch()) {
            	String falseCode = block.getFalseBranch().accept(this);
				childrenCode = childrenCode + falseCode;
            }
        }
        for (CFGLine line: block.getLines()) {
            code += line.accept(this);
        }
        if (! block.isEnd() && block.isBranch() && !blockNames.get(block.getFalseBranch()).equals("null")) {
            code += "mov $0, %r11\n";
            code += "cmp %r11, %r10\n";
            code += "je " + blockNames.get(block.getFalseBranch()) + "\n"; // this line needs to go after the visitor on the falseBranch so the label has been generated
        }
		if(jumpToTrue) {
			code += "jmp " + blockNames.get(block.getTrueBranch()) + "\n";
		}
		if(block.isEnd()) {
			code += "jmp " + label + "_end\n";
		}
        code += childrenCode;
        return code;
    }
}

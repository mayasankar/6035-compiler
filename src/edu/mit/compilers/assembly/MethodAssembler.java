package edu.mit.compilers.assembly;

import java.util.Map;

import edu.mit.compilers.cfg.CFG;
import edu.mit.compilers.cfg.CFGBlock;
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

public class MethodAssembler implements CFGLine.CFGVisitor<List<AssemblyLine>> {

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

    public List<AssemblyLine> assemble(CFG cfg) {
        List<AssemblyLine> prefixLines = new ArrayList<>();
        prefixLines.add(new ALabel(label));

        List<AssemblyLine> lines = pullInArguments();

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

    // NOTE: GUARANTEED TO ONLY USE %r10
    private List<AssemblyLine> onDepthZeroExpression(IRExpression expr) {
        if (expr.getDepth() > 0) {
            throw new RuntimeException("Called onDepthZeroExpression on expression of non-zero depth.");
        }
        return expr.accept(expressionAssembler);
    }

    private List<AssemblyLine> onExpression(IRExpression expr) {
        return expr.accept(expressionAssembler);
    }

    @Override
    public List<AssemblyLine> on(CFGAssignStatement line) {
        IRVariableExpression varAssigned = line.getVarAssigned();
        List<AssemblyLine> lines = onExpression(line.getExpression());  // value now in %r10
        if (varAssigned.isArray()){
            lines.add(new APush("%r10")); // will get it out right before the end and assign to %r11
            lines.addAll(onExpression(varAssigned.getIndexExpression())); // array index now in %r10
            lines.add(new APop("%r11"));
        }
        else {
            lines.add(new AMov("%r10", "%r11"));
        }
        lines.addAll(stacker.moveFrom(varAssigned.getName(), "%r11", "%r10"));
        return lines;
    }

    @Override
    public List<AssemblyLine> on(CFGBoundsCheck line) {
        IRVariableExpression variable = line.getExpression();
        List<AssemblyLine> lines = onExpression(variable.getIndexExpression()); // array index now in %r10
        String indexRegister = "%r10";
        lines.add(new AMov(stacker.getMaxSize(variable.getName()), "%r11"));
        lines.add(new ACmp("%r11", indexRegister));
        lines.add(new AJmp("jge", ".out_of_bounds"));
        lines.add(new ACmp("$0", indexRegister));
        lines.add(new AJmp("jl", ".out_of_bounds"));
        return lines;
    }

    @Override
    public List<AssemblyLine> on(CFGConditional line) {
        return onDepthZeroExpression(line.getExpression());
    }

    @Override
    public List<AssemblyLine> on(CFGNoOp line) {
        return new ArrayList<AssemblyLine>();
    }

    @Override
    public List<AssemblyLine> on(CFGReturn line) {
        List<AssemblyLine> lines = new ArrayList<>();
        if (!line.isVoid()) {
            IRExpression returnExpr = line.getExpression();
            lines.addAll(onDepthZeroExpression(returnExpr));  // return value now in %r10
            lines.add(new AMov("%r10", "%rax"));
        }
        lines.add(new AJmp("jmp", label + "_end")); // jump to end of method where we return
        return lines;
    }

    @Override
    public List<AssemblyLine> on(CFGMethodCall line) {
        List<AssemblyLine> lines = new ArrayList<>();
        IRMethodCallExpression methodCall = line.getExpression();
        List<IRExpression> arguments = methodCall.getArguments();
        List<String> registers = new ArrayList<>(Arrays.asList("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"));
        for (int i=arguments.size()-1; i>=6; i--) {
            // if so many params we need stack pushes: iterate from size-1 down to 6 and push/pop them
            IRExpression arg = arguments.get(i);
            lines.addAll(onDepthZeroExpression(arg));
            lines.add(new APush("%r10"));
        }
        for (int i=0; i<arguments.size() && i < 6; i++) {
            IRExpression arg = arguments.get(i);
            lines.addAll(onDepthZeroExpression(arg));
            lines.add(new AMov("%r10", registers.get(i)));
        }
        lines.add(new AMov("$0", "%rax"));
        lines.add(new ACall(methodCall.getName()));
        for (int i=arguments.size()-1; i>=6; i--) {
            lines.add(new APop("%r10"));
        }
        lines.add(new AMov("%rax", "%r10"));
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
        if (! block.isEnd() && block.isBranch() && !blockNames.get(block.getFalseBranch()).equals("null")) {
            lines.add(new AMov("$0", "%r11"));
            lines.add(new ACmp("%r11", "%r10"));
            lines.add(new AJmp("je", blockNames.get(block.getFalseBranch()))); // this line needs to go after the visitor on the falseBranch so the label has been generated
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

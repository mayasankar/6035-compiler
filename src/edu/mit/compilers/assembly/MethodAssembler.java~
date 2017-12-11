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
        String indexName = expressionAssembler.getExprName(index);
        String indexRegister;
        if(!stacker.isVarStoredInRegister(indexName, line)) {
            indexRegister = stacker.getFirstFreeRegister(line);
            lines.addAll(stacker.moveFromStore(indexName, indexRegister, indexRegister));
        } else {
            indexRegister = stacker.getLocationOfVariable(indexName, line);
        }
        lines.add(new ACmp("$0", indexRegister));
        lines.add(new AJmp("jl", ".out_of_bounds"));

        lines.add(new ACmp(stacker.getMaxSize(variable.getName()), indexRegister));
        lines.add(new AJmp("jge", ".out_of_bounds"));

        return lines;
    }

    @Override
    public List<AssemblyLine> on(CFGConditional line) {
        List<AssemblyLine> lines = new ArrayList<>();
        IRExpression branchExpr = line.getExpression();
        String branchName = expressionAssembler.getExprName(branchExpr);
        String branchLoc;
        if(stacker.isVarStoredInRegister(branchName, line)) {
            branchLoc = stacker.getLocationOfVariable(branchName, line);
        } else {
            branchLoc = stacker.getFirstFreeRegister(line);
            lines.addAll(stacker.moveFromStore(branchName, branchLoc, branchLoc));
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
            String returnName = expressionAssembler.getExprName(returnExpr);
            lines.addAll(stacker.moveFromStore(returnName, "%rax", "%rax"));
        }
        lines.add(new AJmp("jmp", label + "_end")); // jump to end of method where we return
        return lines;
    }

    @Override
    public List<AssemblyLine> on(CFGMethodCall line) {
        expressionAssembler.method = line;
        return line.getExpression().accept(expressionAssembler);
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

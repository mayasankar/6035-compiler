package edu.mit.compilers.assembly;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.cfg.CFGProgram;
import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.cfg.optimizations.*;
import edu.mit.compilers.assembly.lines.*;
import edu.mit.compilers.symbol_tables.VariableDescriptor;
import edu.mit.compilers.ir.decl.IRMethodDecl;

public class AssemblerNew {
	private String code;
	private VariableStackAssigner stacker;

	public AssemblerNew(CFGProgram program) {
		List<AssemblyLine> lines = new ArrayList<>();
		stacker = new VariableStackAssigner(program);
		lines.add(new ACommand(".globl main"));
		lines.add(new AWhitespace());

		for (VariableDescriptor var : program.getGlobalVariables()) {
			String size = Integer.toString(var.getSpaceRequired());
            lines.add(new ACommand(".comm " + var.getName() + ", " + size + ", 8"));
        }

		for(String method: program.getMethodNames()) {
			//System.out.println("Adding code for method: " + method.toString());
		    int numParams = program.getNumParams(method);
		    IRMethodDecl decl = program.getMethodParameters(method);
			TypeDescriptor returnType = program.getMethodReturnType(method);
		    MethodAssembler methodAssembler = new MethodAssembler(method, numParams, stacker, returnType, decl);
			lines.addAll(methodAssembler.assemble(program.getMethodCFG(method)));
		}

		lines.add(new AWhitespace());
		lines.add(new ALabel(".out_of_bounds"));
		lines.add(new AMov("$1", "%eax"));
		lines.add(new AMov("$-1", "%ebx"));
		lines.add(new ACommand("int $0x80"));

		lines.add(new AWhitespace());
		lines.add(new ALabel(".nonreturning_method"));
		lines.add(new AMov("$1", "%eax"));
		lines.add(new AMov("$-2", "%ebx"));
		lines.add(new ACommand("int $0x80"));

		lines = CodeSimplifier.simplifyMovs(lines);

		code = "";
		for (AssemblyLine line : lines) {
			code += line.getString();
		}

	}

	public void printToStream(PrintStream os) throws IOException {
        os.println(code);
	}
}

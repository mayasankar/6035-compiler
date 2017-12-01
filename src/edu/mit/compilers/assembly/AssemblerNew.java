package edu.mit.compilers.assembly;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import edu.mit.compilers.symbol_tables.TypeDescriptor;
import edu.mit.compilers.cfg.CFGProgram;
import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.cfg.optimizations.*;
import edu.mit.compilers.symbol_tables.VariableDescriptor;
import edu.mit.compilers.ir.decl.IRMethodDecl;

public class AssemblerNew {
	private String code;
	private VariableStackAssigner stacker;

	public AssemblerNew(CFGProgram program) {
		stacker = new VariableStackAssigner(program);
		code = ".globl main\n\n";

		for (VariableDescriptor var : program.getGlobalVariables()) {
			//System.out.println("Adding global variable: " + var.toString());
            code += var.toGlobalAssembly();
        }

		for(String method: program.getMethodNames()) {
			//System.out.println("Adding code for method: " + method.toString());
		    int numParams = program.getNumParams(method);
		    IRMethodDecl decl = program.getMethodParameters(method);
			TypeDescriptor returnType = program.getMethodReturnType(method);
		    MethodAssembler methodAssembler = new MethodAssembler(method, numParams, stacker, returnType, decl);
			code += methodAssembler.assemble(program.getMethodCFG(method));
		}

        code += "\n.out_of_bounds:\n";
        code += "mov $1, %eax\n";
        code += "mov $-1, %ebx\n";
        code += "int $0x80\n";

        code += "\n.nonreturning_method:\n";
        code += "mov $1, %eax\n";
        code += "mov $-2, %ebx\n";
        code += "int $0x80\n";

		code = CodeSimplifier.simplifyMovs(code);
	}

	public void printToStream(PrintStream os) throws IOException {
        os.println(code);
	}
}

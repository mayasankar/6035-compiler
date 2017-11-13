package edu.mit.compilers.cfg;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import edu.mit.compilers.symbol_tables.TypeDescriptor;

public class AssemblerNew {
	private String code;
	private VariableStackAssigner stacker;
	
	public AssemblerNew(CFGProgram program) {
		stacker = new VariableStackAssigner(program);
		code = "";
		for(String method: program.getMethodNames()) {
		    int numParams = program.getNumParams(method);
		    TypeDescriptor returnType = program.getMethodReturnType(method);
		    MethodAssembler methodAssembler = new MethodAssembler(method, numParams, stacker, returnType);
			code += methodAssembler.assemble(program.getMethodCFG(method));
		}
		
	}

	public void printToStream(OutputStream os) throws IOException {
		PrintWriter writer = new PrintWriter(os);
		writer.println(code);
	}

}

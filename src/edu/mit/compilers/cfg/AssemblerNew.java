package edu.mit.compilers.cfg;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

public class AssemblerNew {
	private String code;
	private VariableStackAssigner stacker;
	
	public AssemblerNew(CFGProgram program) {
		stacker = new VariableStackAssigner(program);
		code = "";
		for(String method: program.getMethodNames()) {
			code += makeCodeHelper(program.getMethodCFG(method));
		}
		
	}

	public void printToStream(OutputStream os) throws IOException {
		PrintWriter writer = new PrintWriter(os);
		writer.println(code);
	}
	
	private List<String> makeCodeHelper(CFG methodCFG) {
		// TODO Auto-generated method stub
		return null;
	}
}

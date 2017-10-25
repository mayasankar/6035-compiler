package edu.mit.compilers.cfg;

import java.io.OutputStream;
import java.io.*;
import java.util.Map;
import java.util.HashMap;

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




public class Assembler {
    public static void makeCode(Map<String, CFGBlock> methods, OutputStream os, MethodTable table) {
        String code = ".globl main\n";
        try {
            os.write(code.getBytes());
        } catch (IOException e) {
            System.err.println(e);
        }
        for (String methodName : methods.keySet()) {
            CFGBlock graph = methods.get(methodName);
            VariableTable parameters = table.get(methodName).getParameters();
            if (parameters == null){
                // skip imports
                continue;
            }
            int numParams = parameters.getVariableList().size();
            BlockAssembler ba = new BlockAssembler(methodName, numParams);
            code = ba.makeCode(graph, parameters) + "\n";
            try {
                os.write(code.getBytes());
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        return;
    }
}

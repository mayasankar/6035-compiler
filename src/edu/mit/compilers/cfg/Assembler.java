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
        for (String methodName : methods.keySet()) {
            CFGBlock graph = methods.get(methodName);
            VariableTable parameters = table.get(methodName).getParameters();
            int numParams;
            if (parameters == null){
                numParams = 0;
            }
            else {
                numParams = parameters.getVariableList().size();
            }
            BlockAssembler ba = new BlockAssembler(methodName, numParams);
            String code = ba.makeCode(graph) + "\n";
            try {
                os.write(code.getBytes());
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        return;
    }
}

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

// todo list
// makeCode(String label, CFG method)

public class Assembler {
    public static void makeCode(Map<String, CFG> methods, OutputStream os, MethodTable table) {
        for (String methodName : methods.keySet()) {
            CFG graph = methods.get(methodName);
            VariableTable parameters = table.get(methodName).getParameters();
            int numParams;
            if (parameters == null){
                numParams = 0;
            }
            else {
                numParams = parameters.getVariableList().size();
            }
            String code = makeCode(methodName, graph, numParams) + "\n";
            try {
                os.write(code.getBytes());
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        return;
    }

    private static String makeCode(String label, CFG method, int numParams) {
        String prefix = label + ":\n";
        String code = "";
        // TODO mov input params to stack
        int numAllocs = numParams; // TODO increment numAllocs as we do stuff that needs new local vars / temps

        CFGLine line = method.getStart();
        while (line != null) {
            //TODO make it iterate through lines and generate code
        }

        String allocSpace = (new Integer(8*numAllocs)).toString();
        code += "leave\n" + "ret\n";
        prefix += "enter $" + allocSpace + ", $0\n";
        return prefix + code;
    }

    private static String makeCodeLine() {
        // TODO
        throw new RuntimeException("Unimplemented");
    }
}

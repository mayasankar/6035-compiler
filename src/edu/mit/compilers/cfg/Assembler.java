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
// makeCodeLineRecursively


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
            String code = makeCode(methodName, graph, numParams) + "\n";
            try {
                os.write(code.getBytes());
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        return;
    }

    private static String makeCode(String label, CFGBlock method, int numParams) {
        String prefix = label + ":\n";
        String code = "";
        // TODO mov input params to stack
        int numAllocs = numParams; // TODO increment numAllocs as we do stuff that needs new local vars / temps

        CFGBlock block = method;
        code += makeCodeLineRecursively(block);

        String allocSpace = (new Integer(8*numAllocs)).toString();
        code += "leave\n" + "ret\n";
        prefix += "enter $" + allocSpace + ", $0\n";
        return prefix + code;
    }

    private static String makeCodeLineRecursively(CFGBlock block) {
        // TODO implement; also please make it use blocks once those work so we don't jump a gazillion times
        // label the location with line.getLabel() (basically just labels with the line's hashcode)
        // make the code for its statement (split cases for CFGMethodDecl, CFGDecl, CFGNoOp, CFGStatement, CFGExpression)
        // if branches, make code for each child, and have jump statements depending true or falseBranch
        // otherwise just make code for the one child and don't need to jump
        throw new RuntimeException("Unimplemented");
    }
}

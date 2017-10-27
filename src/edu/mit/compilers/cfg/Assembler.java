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
    public void makeCode(Map<String, CFGBlock> methods, OutputStream os, MethodTable table, VariableTable globals) {
        String code = ".globl main\n";

        int allocCount = 0;
        VariableTable globalsOnStack = new VariableTable();
        for (VariableDescriptor var : globals.getVariableDescriptorList()) {
            globalsOnStack.add(var);
            allocCount += var.getLength();
        }
        code += "enter $"  + new Integer(8*allocCount).toString() + ", $0\n";

        try {
            os.write(code.getBytes());
        } catch (IOException e) {
            System.err.println(e);
        }
        for (String methodName : methods.keySet()) {
            CFGBlock graph = methods.get(methodName);
            IRMethodDecl md = table.get(methodName);
            if (md.isImport()) { // skip imports
                continue;
            }
            VariableTable parameters = md.getParameters();
            int numParams = parameters.getVariableList().size();
            BlockAssembler ba = new BlockAssembler(methodName, numParams, globalsOnStack);
            code = ba.makeCode(graph, parameters) + "\nleave\n";
            try {
                os.write(code.getBytes());
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        code = "\n.out_of_bounds:\n";
        code += "mov $-1, %rax\n";  // TODO how do we actually exit with error code -1???  internet suggests mov 60, eax; mov -1, edi
        code += "leave\nret\n";
        code += "\n.nonreturning_method:\n";
        code += "mov $-2, %rax\n";
        code += "leave\nret\n";
        try {
            os.write(code.getBytes());
        } catch (IOException e) {
            System.err.println(e);
        }
        return;
    }
}

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
/*
    private static String makeCodeLine(CFGBlock block, String blockLabel) {
        String code = block.getLabel() + ":\n";
        for (CFGLine line : block.getLines()) {
            System.out.println(line.ownValue());
            /*if (line instanceof CFGNoOp) {
                code += "";
            }
            else if (line instanceof CFGDecl) {
                code += makeCodeCFGDecl(line);
            }
            else if (line instanceof CFGExpression) {

            }
            else if (line instanceof CFGMethodDecl) {

            }
            else if (line instanceof CFGStatement) {

            }
            else {
                throw new RuntimeException("CFGLine of unaccepted type.");
            }*
        }

        // TODO implement; also please make it use blocks once those work so we don't jump a gazillion times
        // label the location with line.getLabel() (basically just labels with the line's hashcode)
        // make the code for its statement (split cases for CFGMethodDecl, CFGDecl, CFGNoOp, CFGStatement, CFGExpression)
        // if branches, make code for each child, and have jump statements depending true or falseBranch
        // otherwise just make code for the one child and don't need to jump
        return code;
    }*/

    /*private static String makeCodeCFGDecl(CFGDecl line){

    }
    private static String makeCodeCFGMethodDecl(CFGMethodDecl line){

    }
    private static String makeCodeCFGStatement(CFGStatement line){

    }
    private static String makeCodeCFGExpression(CFGExpression line){

    }*/
}

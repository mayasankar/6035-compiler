package edu.mit.compilers.symbol_tables;

import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;

public class VariableDescriptor implements Named {
    // TODO (mayars) remove, so we can separate symbol tables from IR
    IRMemberDecl declaration;

    // String name;
    // TypeDescriptor type;
    // int spaceRequired;
    int stackOffset;

    public VariableDescriptor(IRMemberDecl decl) {
        declaration = decl;
    }

    public TypeDescriptor getType() { return declaration.getType(); }

    // TODO should be removed
    public IRMemberDecl getDecl() { return declaration; }

    public int getStackOffset() { return stackOffset; }
	public String getName() { return declaration.getName(); }
    public int getLength() { return declaration.getLength(); }

    // returns the new value of rsp after putting this var on the stack
    public int pushOntoStack(int rsp) {
        stackOffset = rsp + declaration.getSpaceRequired();
        return stackOffset;
    }

    // TODO (mayars) fix
    @Override
    public String toString() {
        return declaration.toString() + " (rsp: " + stackOffset + ")";
    }

    public String toGlobalAssembly() {
        return ".comm " + declaration.getName() + ", " + declaration.getSpaceRequired() + ", 8\n";
    }
}

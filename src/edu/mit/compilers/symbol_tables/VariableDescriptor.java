package edu.mit.compilers.symbol_tables;

import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;

public class VariableDescriptor implements Named {
    // TODO (mayars) remove, so we can separate symbol tables from IR
    IRMemberDecl declaration;

    // String name;
    // IRType.Type type;
    // int spaceRequired;
    int stackOffset;

    public VariableDescriptor(IRMemberDecl decl) {
        declaration = decl;
    }

    public IRType.Type getType() { return declaration.getType(); }

    // TODO should be removed
    public IRMemberDecl getDecl() { return declaration; }

	public String getName() { return declaration.getName(); }

    // returns the new value of rsp after putting this var on the stack
    public int pushOntoStack(int rsp) {
        stackOffset = rsp;
        return rsp + declaration.getSpaceRequired();
    }

    // TODO (mayars) fix
    @Override
    public String toString() {
        return "rsp: " + stackOffset + "; " + declaration.toString();
    }
}

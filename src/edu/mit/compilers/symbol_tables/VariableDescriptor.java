package edu.mit.compilers.symbol_tables;

import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;

public class VariableDescriptor implements Named {

    String name;
    TypeDescriptor type;
    int length;
    int stackOffset;
    IRMemberDecl decl; // TODO I tried to remove this like the previous TODO said, but I'm pretty sure it's too deeply ingrained in SemanticCheckerVisitor. -jamb

    public VariableDescriptor(IRMemberDecl decl) {
        this.name = decl.getName();
        this.type = decl.getType();
        this.length = decl.getLength();
        this.decl = decl;
    }

    // NOTE only use this at CFG; cannot be used for array variables; assumes you'll never actually need decl
    public VariableDescriptor(String name) {
        // TODO do we break anything by claiming this is always INT? (should be fine at CFG)
        this.name = name;
        this.type = TypeDescriptor.INT;
        this.length = 0;
        this.decl = null;
    }

    public VariableDescriptor(String name, int length) {
        this.name = name;
        this.type = TypeDescriptor.array(TypeDescriptor.INT);
        this.length = length;
        this.decl = null;
    }

    public TypeDescriptor getType() { return type; }

    public int getStackOffset() { return stackOffset; }
	public String getName() { return name; }
    public int getLength() { return length; }
    public boolean isArray() { return type.isArray(); }
    public int getSpaceRequired() { return 8 * (isArray() ? length : 1); }
    public IRMemberDecl getDecl() { return decl; }

    // returns the new value of rsp after putting this var on the stack
    public int pushOntoStack(int rsp) {
        stackOffset = rsp + getSpaceRequired();
        return stackOffset;
    }

    // TODO (mayars) fix
    @Override
    public String toString() {
        return name + " (rsp: " + stackOffset + ")";
    }
}

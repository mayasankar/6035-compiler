package edu.mit.compilers.symbol_tables;

import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;

public class VariableDescriptor implements Named {

    String name;
    TypeDescriptor type;
    int length;
    int stackOffset;
    String register = "";
    boolean inStack = true;
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

    // returns a copied VariableDescriptor with newName
    public VariableDescriptor(String newName, VariableDescriptor other) {
        this.name = newName;
        this.type = other.type;
        this.length = other.length;
        this.decl = null; // TODO do we need it to be accessible?
    }

    public void resetName() { this.name = decl.getName(); }

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

    public void putInRegister(String register) {
        this.register = register;
        this.inStack = false;
    }

    public boolean inRegister() {
        return !inStack;
    }

    public String getRegister() {
        if (register.equals("")) {
            throw new RuntimeException("Attempted to get register that hasn't been assigned.");
        }
        return register;
    }

    // TODO (mayars) fix
    @Override
    public String toString() {
        return name + " (rsp: " + stackOffset + ")";
    }
}

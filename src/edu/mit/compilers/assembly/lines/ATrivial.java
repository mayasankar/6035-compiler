package edu.mit.compilers.assembly.lines;

public class ATrivial extends AssemblyLine {

    @Override
    public String getString() {
        return "";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
